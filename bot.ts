import { Bot, Context } from "https://deno.land/x/grammy@v1.9.0/mod.ts";
import { Menu } from "https://deno.land/x/grammy_menu@v1.1.2/mod.ts";
import { NotFoundError, fetchTLDR } from "./fetch-tldr.ts";

const BOT_TOKEN = Deno.env.get("BOT_TOKEN");
if (BOT_TOKEN === undefined) {
  throw new Error("No `BOT_TOKEN` environment variable provided");
}

export const bot = new Bot(BOT_TOKEN);

const usersInput = new Map<number, Set<string>>();

const menu = new Menu("select-command-menu").dynamic((ctx, range) => {
  const userId = ctx.from?.id;
  if (!userId) {
    return;
  }
  const commands = usersInput.get(userId) ?? new Set();
  for (const command of commands) {
    range.text(command, (ctx) => {
      respond(ctx, command);
    });
  }
  return range;
});

bot.use(menu);

bot.command("start", (ctx) => {
  ctx.reply("Welcome! Type a command to get help with");
});

bot.on("message:text", (ctx) => {
  const command = ctx.message.text;
  const multipleCommands = /\s/gi.test(command);
  if (multipleCommands) {
    return respondChoose(ctx);
  }
  return respond(ctx, command);
});

function respondChoose(ctx: Context) {
  const commandRaw = ctx.message?.text?.toLocaleLowerCase() ?? "";
  const commands = commandRaw.split(/\s/).filter(Boolean).slice(0, 10);
  const userId = ctx.from?.id;
  if (!userId) {
    return;
  }
  usersInput.set(userId, new Set(commands));
  ctx.reply(`Choose only one command:`, {
    reply_markup: menu,
  });
}

async function respond(ctx: Context, commandRaw: string) {
  const command = commandRaw.toLocaleLowerCase();
  console.log(`Requested command: ${command}`);
  try {
    const tldr = await fetchTLDR(command);
    return ctx.reply(tldr);
  } catch (e) {
    if (e instanceof NotFoundError) {
      return ctx.reply(`Help for "${command}" not found!`);
    } else {
      return ctx.reply("Sorry, internal error");
    }
  }
}
