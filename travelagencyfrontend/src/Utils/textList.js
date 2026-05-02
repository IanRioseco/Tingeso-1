// Modelo de formulario de perfil con valores predeterminados.
const parseList = (text, separator) => {
  if (!text) {
    return [];
  }
  // Parsea una lista de valores separados por un separador.
  return text
    .split(separator)
    .map((item) => item.trim())
    .filter((item) => item.length > 0);
};
// Parsea una lista de valores separados por una coma.
export const parseCommaList = (text) => parseList(text, ',');
// Parsea una lista de valores separados por un punto.
export const parseDotList = (text) => parseList(text, '.');
// Parsea una lista de valores separados por un punto y coma.
export const parseSemicolonList = (text) => parseList(text, ';');
