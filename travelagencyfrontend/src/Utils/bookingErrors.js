import { extractApiErrorMessage } from './apiError';

// Crea un mensaje de error amigable para los errores de reserva.
export const getFriendlyBookingError = (error, passengers) => {
  // Extrae el mensaje de error de la respuesta de la API.
  const rawMessage = extractApiErrorMessage(error);
  // Normaliza el mensaje de error.
  const normalized = rawMessage.toLowerCase();

  // Si el mensaje de error indica que la reserva está activa, devuelve un mensaje de error amigable.
  if (normalized.includes('reserva activa para este paquete')) {
    return 'No puedes reservar este paquete otra vez porque ya tienes una reserva activa (pendiente o confirmada). Si quieres volver a reservarlo, primero cancela la actual o espera a que expire.';
  }
  // Si el mensaje de error indica que no hay suficientes cupos, devuelve un mensaje de error amigable.
  if (normalized.includes('cupos disponibles')) {
    return `No hay suficientes cupos para ${passengers} pasajero(s). Ajusta la cantidad e intentalo nuevamente.`;
  }
  // Si el mensaje de error indica que el paquete no existe, devuelve un mensaje de error amigable.
  if (normalized.includes('paquete') && (normalized.includes('no encontrada') || normalized.includes('not found'))) {
    return 'No pudimos identificar el paquete para esta reserva. Vuelve al detalle del paquete e intentalo de nuevo.';
  }

  return rawMessage || 'No se pudo crear la reserva.';
};
