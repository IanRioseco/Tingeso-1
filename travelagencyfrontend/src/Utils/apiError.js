// Extrae el mensaje de error de la respuesta de la API.
export const extractApiErrorMessage = (error) => {
  // Extrae el mensaje de error de la respuesta de la API.
  const data = error?.response?.data;

  // Si no hay respuesta de la API, devuelve un mensaje de error genérico.
  if (!data) {
    return '';
  }
  // Si el mensaje de error es una cadena, devuelve el mensaje de error.
  if (typeof data === 'string') {
    return data;
  }
  // Si el mensaje de error es un objeto con una propiedad 'error', devuelve el mensaje de error.
  if (typeof data.error === 'string') {
    return data.error;
  }
  // Si el mensaje de error es un objeto con una propiedad 'message', devuelve el mensaje de error.
  if (typeof data.message === 'string') {
    return data.message;
  }
  // Si el mensaje de error es un objeto, devuelve el primer valor que sea una cadena.
  if (typeof data === 'object') {
    const firstValue = Object.values(data).find((value) => typeof value === 'string');
    return firstValue || '';
  }

  return '';
};
