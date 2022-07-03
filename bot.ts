import { Bot } from "https://deno.land/x/grammy@v1.9.0/mod.ts";
import { NotFoundError, fetchTLDR } from "./fetch-tldr.ts";

const BOT_TOKEN = Deno.env.get("BOT_TOKEN");
if (BOT_TOKEN === undefined) {
  throw new Error("No `BOT_TOKEN` environment variable provided");
}

export const bot = new Bot(BOT_TOKEN); // <-- place your bot token inside this string

bot.command("start", (ctx) =>
  ctx.reply("Welcome! Type a command to get help with")
);

bot.on("message:text", async (ctx) => {
  const command = ctx.message.text;
  if (!command) {
    return ctx.reply("Specify command");
  }
  try {
    const tldr = await fetchTLDR(command);
    return ctx.reply(tldr);
  } catch (e) {
    if (e instanceof NotFoundError) {
      return ctx.reply(`Help for "${command}" not found`);
    } else {
      return ctx.reply("Sorry, internal error");
    }
  }
});
