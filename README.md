# Hurricane
A rich command system API using methods. Just write a method, and the API will build a command for it from its paramters!

Easy to use, elegant and efficient!

## What do you mean?

This API was created to make it easier to create commands for various projects needing a system to handle user or operator commands. Such systems are useful often in Discord bots and Minecraft plugins.

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

# Annotation Adapters

An annotation adapter is a class defining the behavior of an annotation type when used on a command method or on a parameter in a command method.

## Method Annotation Adapters

These annotations are used on the methods themselves, and can modify their registration process, before they are executed and after they are executed.

### A list of some builtin Method Annotation Adapters:

### `@Command`

This annotation is necessary for each method you want to be automatically registered when registering its containing class.

It can also be used to register tree commands, commands that have multiple sub-commands.

Tree commands are defined with an inner class annotated with `@Command`, and all methods inside (with that annotation too) will be sub commands.

This annotation can optionally change the name of a command from the method or class's name.

### `@Feedback`

When this annotation is used, after the method is executed it will send the sender the message defined in by the annotation's `success` or `fail` values.

A failure of a command is when a method has been executed and threw an exception. Additionally, if the method returned the value `false`, it will be considered failed too. Any other return value, including `null` and `void` will be a success.

The `success` and `fail` messages can include the returned value with the `${result}` pattern, and can include any parameter value with `${paramName}`. You can also chain simple calls to the value, for example:
`${param.getSomething().field}`.

## Parameter Annotation Adapters

These annotations are used on parameters in a method command. They can modify the registration process, and the value parsed from the input.

### A list of some builtin Param Annotation Adapters:

Soon™