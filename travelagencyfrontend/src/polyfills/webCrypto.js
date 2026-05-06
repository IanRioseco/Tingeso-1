const createFallbackRandomBytes = (length) => {
  const bytes = new Uint8Array(length);
  for (let index = 0; index < length; index += 1) {
    bytes[index] = Math.floor(Math.random() * 256);
  }
  return bytes;
};

const createFallbackUuid = () => {
  const bytes = createFallbackRandomBytes(16);
  bytes[6] = (bytes[6] & 0x0f) | 0x40;
  bytes[8] = (bytes[8] & 0x3f) | 0x80;

  const hex = Array.from(bytes, (byte) => byte.toString(16).padStart(2, '0')).join('');
  return `${hex.slice(0, 8)}-${hex.slice(8, 12)}-${hex.slice(12, 16)}-${hex.slice(16, 20)}-${hex.slice(20)}`;
};

const installPolyfill = () => {
  const existingCrypto = globalThis.crypto ?? window.crypto ?? {};

  if (typeof existingCrypto.getRandomValues !== 'function') {
    existingCrypto.getRandomValues = (typedArray) => {
      const fallbackBytes = createFallbackRandomBytes(typedArray.length);
      typedArray.set(fallbackBytes);
      return typedArray;
    };
  }

  if (typeof existingCrypto.randomUUID !== 'function') {
    existingCrypto.randomUUID = createFallbackUuid;
  }

  if (typeof existingCrypto.subtle === 'undefined') {
    existingCrypto.subtle = {
      digest: async () => createFallbackRandomBytes(32).buffer,
    };
  }

  try {
    globalThis.crypto = existingCrypto;
  } catch {
    // En algunos navegadores la propiedad es de solo lectura; en ese caso nos quedamos con el objeto existente.
  }

  if (typeof window !== 'undefined') {
    try {
      window.crypto = existingCrypto;
    } catch {
      // No-op si el navegador impide reasignar window.crypto.
    }
  }
};

installPolyfill();
