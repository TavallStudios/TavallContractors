import path from "node:path";
import { readdir, readFile, writeFile } from "node:fs/promises";
import { minify } from "terser";

const jsRoot = path.resolve("src/main/resources/static/js");

async function listFiles(dir) {
  const entries = await readdir(dir, { withFileTypes: true });
  const files = [];

  for (const entry of entries) {
    const fullPath = path.join(dir, entry.name);
    if (entry.isDirectory()) {
      files.push(...(await listFiles(fullPath)));
      continue;
    }

    files.push(fullPath);
  }

  return files;
}

async function minifyCompiledFiles() {
  let files = [];
  try {
    files = await listFiles(jsRoot);
  } catch (error) {
    if (error && error.code === "ENOENT") {
      console.log("No compiled JS directory found, skipping minification.");
      return;
    }
    throw error;
  }

  const jsFiles = files.filter((file) => file.endsWith(".js") && !file.endsWith(".min.js"));

  for (const jsFile of jsFiles) {
    const source = await readFile(jsFile, "utf8");
    const result = await minify(source, {
      compress: true,
      mangle: true,
      format: { comments: false }
    });

    if (!result.code) {
      throw new Error(`Failed to minify ${jsFile}`);
    }

    const minifiedFile = jsFile.replace(/\.js$/, ".min.js");
    await writeFile(minifiedFile, result.code, "utf8");
    console.log(`Minified ${path.relative(jsRoot, jsFile)} -> ${path.relative(jsRoot, minifiedFile)}`);
  }

  console.log(`Minified ${jsFiles.length} JavaScript file(s).`);
}

await minifyCompiledFiles();
