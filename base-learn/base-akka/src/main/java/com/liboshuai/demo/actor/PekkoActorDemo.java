package com.liboshuai.demo.actor;


import org.apache.pekko.actor.AbstractActor;
import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.ActorSystem;
import org.apache.pekko.actor.Props;
import org.apache.pekko.pattern.Patterns;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.io.Serializable;
import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

/**
 * 演示使用 Pekko Actor 模型来管理并发库存。
 * *
 * 业务场景：与 TraditionalLockDemo 相同。
 * - 初始库存 100 件。
 * - 150 个并发用户抢购，每个用户购买 1 件。
 * *
 * Actor 模型的核心思想：
 * 1.  **封装状态**：库存数量 (stock) 被封装在 InventoryActor 内部，外部无法直接访问。
 * 2.  **消息传递**：对库存的任何操作（如“购买”）都必须通过发送异步消息 (PurchaseMessage) 来完成。
 * 3.  **串行处理**：ActorSystem 保证一个 Actor 实例一次只处理一条消息。所有 PurchaseMessage
 * 都会进入该 Actor 的邮箱排队，然后被一个个串行处理。
 * *
 * 这种方式天然地避免了资源竞争，因此**不需要任何锁**。
 */
public class PekkoActorDemo {

    // --- 1. 定义消息 ---
    // 消息必须是不可变的 (immutable) 和可序列化的 (Serializable)。

    /**
     * 基础消息接口，用于 Jackson 序列化绑定
     */
    public interface Message extends Serializable {
    }

    /**
     * 购买消息
     */
    public static class PurchaseMessage implements Message {
        private static final long serialVersionUID = 1L;
        public final int amount;
        public final String customerName;

        @JsonCreator // 标记为 Jackson 的构造函数
        public PurchaseMessage(int amount, String customerName) {
            this.amount = amount;
            this.customerName = customerName;
        }
    }

    /**
     * 用于查询当前库存的消息 (通常用于验证/调试)
     */
    public static class GetStockMessage implements Message {
        private static final long serialVersionUID = 1L;
    }

    // --- 2. 定义 Actor ---

    /**
     * 库存 Actor
     * * 这个 Actor 是 'stock' 状态的唯一"守护者"。
     * 它的内部状态 (stock) 只能被它自己的 receive 方法访问。
     */
    public static class InventoryActor extends AbstractActor {

        private int stock;

        public InventoryActor(int initialStock) {
            this.stock = initialStock;
        }

        /**
         * 工厂方法，用于创建 Actor
         */
        public static Props props(int initialStock) {
            return Props.create(InventoryActor.class, () -> new InventoryActor(initialStock));
        }

        /**
         * 消息处理逻辑
         */
        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .match(PurchaseMessage.class, this::handlePurchase)
                    .match(GetStockMessage.class, this::handleGetStock)
                    .build();
        }

        /**
         * 处理购买请求
         */
        private void handlePurchase(PurchaseMessage msg) {
            String customerName = msg.customerName;
            int amount = msg.amount;

            System.out.println(customerName + " 尝试购买 " + amount + "件... " + "当前库存: " + stock);

            // 因为 Actor 串行处理消息，所以这里的 "if-then-else" 块是原子性的
            // 不需要加锁！
            if (stock >= amount) {
                // 模拟处理耗时
                try {
                    // 注意：在 Actor 中， Thread.sleep() 是一个坏习惯！
                    // 它会阻塞 Actor 线程，使其无法处理其他消息。
                    // 在真实应用中，这里应该是一个异步非阻塞操作 (e.g., call external API)。
                    // 但为了与 Lock 示例进行公平对比（它也sleep了10ms），我们在这里保留它。
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                stock -= amount;
                System.out.println("-> " + customerName + " 购买成功! " + "剩余库存: " + stock);
                // (可选) 可以给发送者回复一个成功消息
                // getSender().tell(new PurchaseSuccess(), getSelf());
            } else {
                System.out.println("-> " + customerName + " 购买失败! " + "库存不足 (仅剩 " + stock + "件).");
                // (可选) 可以给发送者回复一个失败消息
                // getSender().tell(new PurchaseFailed("Stock not enough"), getSelf());
            }
        }

        /**
         * 处理库存查询请求
         */
        private void handleGetStock(GetStockMessage msg) {
            // 使用 tell 将当前库存 (stock) 作为消息回复给 "ask" 的发送者
            getSender().tell(stock, getSelf());
        }
    }


    // --- 3. 主程序 (启动器) ---
    public static void main(String[] args) {
        // 1. 创建 Actor 系统
        // 它会自动加载 src/main/resources/application.conf 文件
        final ActorSystem system = ActorSystem.create("InventorySystem");

        // 2. 创建 InventoryActor
        // 传入初始库存 100
        final ActorRef inventoryActor = system.actorOf(InventoryActor.props(100), "inventory-actor");

        int numberOfCustomers = 150;
        int purchaseAmountPerCustomer = 1;

        System.out.println("--- 并发抢购模拟开始 (Pekko Actor) ---");
        System.out.println("初始库存: 100");
        System.out.println("模拟并发用户数: " + numberOfCustomers);
        System.out.println("---------------------------------");

        // 3. 模拟 150 个并发购买请求
        // 我们使用 'tell' (fire-and-forget)，这是一个完全异步的操作。
        // 所有150条消息会几乎同时被发送到 Actor 的邮箱中排队。
        for (int i = 0; i < numberOfCustomers; i++) {
            PurchaseMessage purchaseMsg = new PurchaseMessage(purchaseAmountPerCustomer, "顾客-" + (i + 1));
            inventoryActor.tell(purchaseMsg, ActorRef.noSender()); // ActorRef.noSender()
            // 表示我们不关心回复
        }

        // 4. 等待所有购买消息处理完毕，并获取最终库存
        // 我们使用 'ask' 模式。这条 GetStockMessage 会被排在所有
        // PurchaseMessage 之后处理。
        System.out.println("---------------------------------");
        System.out.println("...所有购买请求已发送，等待 Actor 处理完毕并查询最终库存...");

        // 'ask' 会返回一个 Future (CompletionStage)
        Duration timeout = Duration.ofSeconds(10);
        CompletionStage<Object> future = Patterns.ask(inventoryActor, new GetStockMessage(), timeout);

        try {
            // 阻塞等待 'ask' 的回复
            Integer finalStock = (Integer) future.toCompletableFuture().get(10, TimeUnit.SECONDS);

            System.out.println("--- 并发抢购模拟结束 (Pekko Actor) ---");
            System.out.println("最终剩余库存: " + finalStock);

            // 5. 验证结果
            if (finalStock == 0) {
                System.out.println("结果正确: 库存已售罄 (100件已售出, 50次尝试失败)。");
            } else {
                System.out.println("结果错误: 最终库存不为0! (库存 " + finalStock + ")");
            }

        } catch (Exception e) {
            System.err.println("获取最终库存失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 6. 关闭 Actor 系统
            system.terminate();
        }
    }
}
