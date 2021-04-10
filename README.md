# Hurricane
A rich command system API using methods. Just write a method, and the API will build a command for it from its parameters!

Easy to use, elegant and efficient!

## What do you mean?

This API was created to make it easier to create commands for various projects needing a system handling user or operator commands. Such systems are often useful in Discord bots and Minecraft plugins.

For each Java method you give the API, it will generate a command, and when you pass it any input from the user, it will parse and execute the matching method with any additional arguments you define as the method parameters! It's almost like magic!

## Ok that sounds interesting, but how do you use it?

Here is a very simple example of a command that will generate a random number in the given range:

```java
@Command
public int rand(int min, int max) {
    int num = new Random().nextInt(max - min + 1) + min;
    System.out.println("Your number is: " + num);
}
```

In this example we have the command, `rand`, that takes 2 integer arguments. It then generates a random number in that range and prints it.

Very simple indeed.

Then, to register this command, we need to create an instance of `Hurricane`, and pass the containing class to the `register()` method:

```java
import com.shinybunny.hurricane.Hurricane;

class Example {
    public static void main(String[] args) {
        Hurricane api = new Hurricane();
        api.register(new Example());
    }
    
    @Command
    public int rand(int min, int max) {
        int num = new Random().nextInt(max - min + 1) + min;
        System.out.println("Your number is: " + num);
    }
}
```

Note that only methods annotated with the `@Command` annotation will be registered. So don't worry, the `main` method will not be converted to a command :)

Now, if we want to test it, we can call `Hurricane.execute()` with some input.
```java
Hurricane api = new Hurricane();
api.register(new Example());
try {
    api.execute(CommandSender.CONSOLE, "rand 5 10");
} catch (CommandParsingException e) {
    System.err.println("Invalid syntax: " + e.getMessage());
}
```

We use here the `CommandSender.CONSOLE` as a default originator of the command, and pass a command of `rand 5 10`. If everything goes to plan, we should get a random number between 5 and 10.

## Yo that's kinda neat, what else can I do with the API?

This API has many more features than just simple detection and parsing of integers separated by spaces. 

The API uses annotations to modify and validate the input in a fancier way than inside the method body. Other annotations can be used on the method itself to modify its behavior. You can even define your own custom annotation adapters for parameters or methods!

Custom argument adapters can be used to react to different kinds of parameter types, and be able to parse them too!

## An important note on parameter names

When defining a parameter argument, its name defaults to the parameter's name, and this name is often used by error messages. Unless you compile your code with the `-parameters` flag, they default to `arg0`, `arg1` etc. To overcome this, either compile with that flag or use the [@Arg annotation](#arg).

# Annotation Adapters

An annotation adapter is a class defining the behavior of an annotation type when used on a command method or on a parameter in a command method.

## Method Annotation Adapters

These annotations are used on the methods themselves, and can modify their registration process, before they are executed and after they are executed.

### A list of some builtin Method Annotation Adapters:

### `@Command`

This annotation is necessary for each method you want to be automatically registered when registering its containing class.

It can also be used to register tree commands, commands that have multiple sub-commands, when used on the registered class or an inner class.

This annotation can optionally change the name of a command from the method or class's name, and set a description for the command for providing help.

### `@Feedback`

When this annotation is used, after the method is executed it will send the sender the message defined in by the annotation's `success` or `fail` values.

A failure of a command is when a method has been executed and threw an exception. Additionally, if the method returned the value `false`, it will be considered failed too. Any other return value, including `null` and `void` will be a success.

The `success` and `fail` messages can include the returned value with the `${result}` pattern, and can include any parameter value with an [access expression](#access-expressions).

### `@Requirement`

This annotation can define a requirement that needs to be met in order to allow execution of the command. The `value()` property should get a class implementing `Requirement.Callback` with the `boolean check(CommandSender sender)` method. This check is run every time before the command is executed and returning `false` will prevent it from going any further.

## Parameter Annotation Adapters

These annotations are used on parameters in a method command. They can modify the registration process, and the value parsed from the input.

### A list of some builtin Param Annotation Adapters:

### `@Arg`

Use this annotation on a parameter argument to set its name and/or description.
An argument's name is defined by default to the parameter's name, but unless you compile your code with the `-parameters` flag, param names will be `arg0`, `arg1` etc. This annotation lets you define a meaningful name for the argument, so error feedback will be more understandable.

### `@Default`

This annotation can make an argument in a command optional, by defining it a default value when it is omitted.

The annotation has many options but only one is used at a time. Depending on the type of argument, the `@Default` annotation selects the matching option. For example, `int` arguments will use the `integer()` property, `String` will use `string()`, etc.

These properties only exist for primitive types, so for any other value type you should use `@Arg(optional=true)` to make it null by default.

Another option for primitive and non-primitive types alike, is to use `@Default(computed="<access expression>")`, with an [Access Expression](#access-expressions).

### `@Greedy`

The Greedy annotation is for a string argument taking a varying length string, typically as the last argument. Simply annotate a `String` argument with `@Greedy` to let it parse more than one word.

### `@Range`

This annotation can be used on any number to define a range of values it allows. If the input number is not within the range defined by `min()` and `max()`, an exception will be thrown, and the command will not run.

### `@Sender`

This annotation can be used on parameters that should be injected as the sender of the command. If the command sender instance cannot be cast to the argument type, it will fail.

## Access Expressions

An access expression is used by `@Feedback` and `@Default` to get a value from a parameter in a method command. These expressions start with an argument's name (which can have spaces if defined with `@Arg`), followed by an optional chain of method calls and field accesses.

Method calls cannot take any parameters at the moment, but in the future you may use literal values or nested access expressions.

Some examples:
- `arg0`
- `arg1.getSomething()`
- `myParam.myField.getStuff().otherField`
- `param with spaces.getName()`