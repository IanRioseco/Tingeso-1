// Utilidades para gestionar la sesión de reserva del usuario.
const getBookingSessionKey = (userId) => `travelagency-booking-session-id-${userId}`;

// Obtiene o crea un identificador de sesión para el usuario.
export const getOrCreateSessionId = (userId) => {
  // Obtiene o crea un identificador de sesión para el usuario.
  const storageKey = getBookingSessionKey(userId);
  const existing = sessionStorage.getItem(storageKey);
  // Si ya existe un identificador de sesión, devuelve el identificador de sesión.
  if (existing) {
    return existing;
  }

  // Si no existe un identificador de sesión, genera uno aleatorio.
  const sessionId = typeof crypto !== 'undefined' && crypto.randomUUID
    ? crypto.randomUUID()
    : `session-${Date.now()}-${Math.random().toString(16).slice(2)}`;
  // Almacena el identificador de sesión en el almacenamiento local.
  sessionStorage.setItem(storageKey, sessionId);
  return sessionId;
};

// Restablece el identificador de sesión para el usuario.
export const resetBookingSessionForUser = (userId) => {
  sessionStorage.removeItem(getBookingSessionKey(userId));
  return getOrCreateSessionId(userId);
};
