const platforms = [
  "android",
  "common",
  "linux",
  "osx",
  "sunos",
  "windows",
] as const;

type Platform = typeof platforms[number];

const platformsSet = new Set(platforms);

export class NotFoundError extends Error {}

async function fetchByPlatform(platform: Platform, command: string) {
  const response = await fetch(
    `https://raw.githubusercontent.com/tldr-pages/tldr/main/pages/${platform}/${command}.md`
  );
  if (response.status === 404) {
    throw new NotFoundError();
  }
  return response.text();
}

export async function fetchTLDR(command: string): Promise<string> {
  for (const platform of platformsSet) {
    try {
      const tldr = await fetchByPlatform(platform, command);
      return tldr;
    } catch (e) {
      if (e instanceof NotFoundError) {
        continue;
      } else {
        throw e;
      }
    }
  }
  throw new NotFoundError();
}
