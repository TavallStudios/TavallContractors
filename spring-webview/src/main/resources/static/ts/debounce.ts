export function debounce<T extends (...args: never[]) => void>(callback: T, delayMs: number): T {
  let timeout: number | undefined;
  return ((...args: never[]) => {
    if (timeout !== undefined) {
      window.clearTimeout(timeout);
    }
    timeout = window.setTimeout(() => callback(...args), delayMs);
  }) as T;
}

