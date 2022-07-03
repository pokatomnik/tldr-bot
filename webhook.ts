import { Application } from "https://deno.land/x/oak@v10.6.0/mod.ts";
import { webhookCallback } from "https://deno.land/x/grammy@v1.9.0/mod.ts";
import { bot } from "./bot.ts";

const app = new Application(); // or whatever you're using

// Make sure to specify the framework you use.
app.use(webhookCallback(bot, "oak"));

app.listen();
