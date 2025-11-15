package com.liboshuai.demo.optional;

import java.util.Optional;

public class OptionalChapter5 {

    // 辅助类: 模拟一个用户, 它的年龄可能是可选的
    static class User {
        private final String name;
        private final Integer age; // 年龄可能为null

        public User(String name, Integer age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return this.name;
        }

        public Optional<Integer> getAge() {
            return Optional.ofNullable(this.age);
        }
    }

    public static void main(String[] args) {
        System.out.println("--- 1. map(Function): 转换值 ---");
        // map 用于将 Optional<T> 安全地转换为 Optional<U>
        // Lambda 表达式 (T -> U)

        Optional<String> nameOpt = Optional.of("Flink");

        // 示例1: 获取字符串长度 (String -> Integer)
        Optional<Integer> lengthOpt = nameOpt.map(String::length);
        System.out.println("map() 转换值 (有值): " + lengthOpt); // Optional[5]

        // 示例2: 使用方法引用
        Optional<String> upperOpt = nameOpt.map(String::toUpperCase);
        System.out.println("map() 方法引用 (有值): " + upperOpt);  // Optional[FLINK]

        // 示例3: 空 Optional 上使用 map
        Optional<String> emptyOpt = Optional.empty();
        Optional<Integer> emptyLengthOpt = emptyOpt.map(String::length);
        System.out.println("map() 转换值 (为空): " + emptyLengthOpt); // Optional.empty

        System.out.println("\n--- 2. filter(Predicate): 过滤值 ---");
        // filter 用于根据条件 (Predicate) 检查值

        Optional<Integer> valueOpt = Optional.of(120);

        // 示例1: 满足条件 (120 > 100)
        Optional<Integer> filteredOpt1 = valueOpt.filter(v -> v > 100);
        System.out.println("filter() 满足条件: " + filteredOpt1); // Optional[120]

        // 示例2: 不满足条件 (120 < 50)
        Optional<Integer> filteredOpt2 = valueOpt.filter(v -> v < 50);
        System.out.println("filter() 不满足条件: " + filteredOpt2); // Optional.empty

        // 示例3: 在空 Optional 上使用 filter
        Optional<Integer> emptyValueOpt = Optional.empty();
        Optional<Integer> filteredOpt3 = emptyValueOpt.filter(v -> v > 100);
        System.out.println("filter() (为空): " + filteredOpt3); // Optional.empty

        System.out.println("\n--- 3. flatMap(Function): 扁平化转换 ---");
        // flatMap 用于 Lambda 表达式返回的也是 Optional 的情况 (T -> Optional<U>)

        User userWithAge = new User("Tom", 25);
        User userWithoutAge = new User("Jerry", null);

        // 场景A: 错误地使用 map 来获取 Optional<Integer> age
        // userWithAge.getAge() 返回 Optional<Integer>
        // map 会自动再包一层, 导致 Optional<Optional<Integer>>
        Optional<Optional<Integer>> nestedOptional = Optional.of(userWithAge).map(User::getAge);
        System.out.println("错误使用 map 导致嵌套: " + nestedOptional); // Optional[Optional[25]]

        // 场景B: 正确地使用 flatMap
        // flatMap 会"解包", 直接返回内层的 Optional<Integer>
        Optional<Integer> ageOpt1 = Optional.of(userWithAge).flatMap(User::getAge);
        System.out.println("flatMap (有年龄): " + ageOpt1); // Optional[25]

        // 场景C: userWithoutAge.getAge() 返回 Optional.empty
        Optional<Integer> ageOpt2 = Optional.of(userWithoutAge).flatMap(User::getAge);
        System.out.println("flatMap (无年龄): " + ageOpt2); // Optional.empty

        System.out.println("\n--- 4. 链式调用 (综合示例) ---");
        // 目标: 获取一个 User 对象, 如果它存在, 并且年龄大于 18 岁, 就返回它的名字的大写形式, 否则返回 "GUEST"

        Optional<User> userOpt = Optional.of(new User("Alice", 20)); // ALICE
//         Optional<User> userOpt = Optional.of(new User("Bob", 15)); // GUEST
//         Optional<User> userOpt = Optional.of(new User("Charlie", null)); // GUEST
//         Optional<User> userOpt = Optional.empty(); // GUEST

        String result = userOpt
                .flatMap(User::getAge)
                .filter(age -> age > 18)
                .flatMap(age -> userOpt.map(User::getName))
                .map(String::toUpperCase)
                .orElse("GUEST");

        System.out.println("链式调用最终结果: " + result);
    }
}
