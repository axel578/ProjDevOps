package com.example.Argent.controller;

import org.springframework.web.bind.annotation.*;


import java.util.concurrent.atomic.AtomicLong;


@RestController
public class GreetingController {

   private static final String template = "Hello, %s!";
   private final AtomicLong counter = new AtomicLong();

   @GetMapping("/")
   public Greeting greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
       return new Greeting(counter.incrementAndGet(), String.format(template, name));
   }

   class Greeting {

       private final long id;
       private final String content;

       public Greeting(long id, String content) {
           this.id = id;
           this.content = content;
       }

       public long getId() {
           return id;
       }

       public String getContent() {
           return content;
       }
   }
}
