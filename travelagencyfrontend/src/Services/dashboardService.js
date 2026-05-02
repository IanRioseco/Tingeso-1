import userService from './UserService';
import packageService from './packageService';
import bookingService from './bookingService';

const getBookingAmount = (booking) => Number(booking.finalAmount ?? booking.totalPrice ?? 0);

const getBookingPassengers = (booking) => Number(booking.passengers ?? booking.numberOfPassengers ?? 1);

// Calcula el número de paquetes por estado de reserva.
const countByStatus = (bookings) => ({
  PENDING: bookings.filter((booking) => booking.bookingStatus === 'PENDING').length,
  CONFIRMED: bookings.filter((booking) => booking.bookingStatus === 'CONFIRMED').length,
  COMPLETED: bookings.filter((booking) => booking.bookingStatus === 'COMPLETED').length,
  CANCELLED: bookings.filter((booking) => booking.bookingStatus === 'CANCELLED').length,
});

// Calcula el total de ingresos de los paquetes.
const calculateTotalRevenue = (bookings) => {
  return bookings
    .filter((booking) => booking.bookingStatus === 'CONFIRMED' || booking.bookingStatus === 'COMPLETED')
    .reduce((sum, booking) => sum + getBookingAmount(booking), 0);
};

// Ordena los paquetes por el número de reservas y el ingreso total.
const buildTopPackages = (packages, bookings) => {
  return packages
    .map((pkg) => {
      const pkgBookings = bookings.filter((booking) => booking.packageId === pkg.id);
      const revenue = pkgBookings
        .filter((booking) => booking.bookingStatus === 'CONFIRMED' || booking.bookingStatus === 'COMPLETED')
        .reduce((sum, booking) => sum + getBookingAmount(booking), 0);
      // Devuelve el paquete con su número de reservas y su ingreso total.
      return {
        ...pkg,
        reservationCount: pkgBookings.length,
        revenue,
      };
    })
    .sort((a, b) => b.reservationCount - a.reservationCount)
    .slice(0, 3);
};

// Calcula el ocupación de los paquetes por número de pasajeros.
const buildPackageOccupancy = (packages, bookings) => {
  return packages
    .map((pkg) => {
      const reserved = bookings
        .filter(
          (booking) =>
            booking.packageId === pkg.id &&
            (booking.bookingStatus === 'CONFIRMED' || booking.bookingStatus === 'COMPLETED')
        )
        .reduce((sum, booking) => sum + getBookingPassengers(booking), 0);
      // Calcula el porcentaje de ocupación.
      const total = pkg.totalSlots || 0;
      const percentage = total > 0 ? (reserved / total) * 100 : 0;
      // Devuelve el paquete con su ocupación.
      return {
        id: pkg.id,
        name: pkg.name,
        reserved,
        total,
        percentage: Math.min(percentage, 100),
      };
    })
    .filter((occupancy) => occupancy.total > 0);
};

// Ordena los últimos reservas y devuelve los detalles de los paquetes.
const buildRecentBookings = (bookings, packages) => {
  return [...bookings]
    .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
    .slice(0, 5)
    .map((booking) => ({
      ...booking,
      packageName: packages.find((pkg) => pkg.id === booking.packageId)?.name || 'N/A',
    }));
};

// Calcula el tasa de conversión de los paquetes.
const calculateConversionRate = (bookings) => {
  const completed = bookings.filter((booking) => booking.bookingStatus === 'COMPLETED').length;
  return bookings.length > 0 ? (completed / bookings.length) * 100 : 0;
};

// Crea alertas para los paquetes con un porcentaje de ocupación elevado.
const buildAlerts = (bookingsByStatus, packageOccupancy) => {
  const alerts = [];
  // Crea una alerta para los paquetes con un porcentaje de ocupación elevado.
  packageOccupancy.forEach((occupancy) => {
    if (occupancy.percentage >= 80 && occupancy.percentage < 100) {
      alerts.push({
        type: 'warning',
        message: `${occupancy.name}: ${occupancy.reserved}/${occupancy.total} cupos (${occupancy.percentage.toFixed(0)}%)`,
      });
    }
  });

  // Crea una alerta para las reservas pendientes.
  const pending = bookingsByStatus.PENDING || 0;
  if (pending > 0) {
    alerts.push({
      type: 'info',
      message: `${pending} reserva(s) pendiente(s) de confirmar`,
    });
  }

  return alerts;
};

// Obtiene los datos de los paquetes, usuarios y reservas.
export const getDashboardStats = async () => {
  const [usersResponse, packagesResponse, bookingsResponse] = await Promise.all([
    userService.getAll(),
    packageService.getAll(),
    bookingService.getAll(),
  ]);

  // Obtiene los datos de los paquetes, usuarios y reservas.
  const bookings = bookingsResponse.data || [];
  const packages = packagesResponse.data || [];
  const users = usersResponse.data || [];

  // Calcula los datos de los paquetes, usuarios y reservas.
  const bookingsByStatus = countByStatus(bookings);
  const totalRevenue = calculateTotalRevenue(bookings);
  const topPackages = buildTopPackages(packages, bookings);
  const packageOccupancy = buildPackageOccupancy(packages, bookings);
  const recentBookings = buildRecentBookings(bookings, packages);
  const conversionRate = calculateConversionRate(bookings);
  const alerts = buildAlerts(bookingsByStatus, packageOccupancy);

  // Devuelve los datos de los paquetes, usuarios y reservas.
  return {
    totalUsers: users.length,
    totalPackages: packages.length,
    totalBookings: bookings.length,
    totalBookingsCancelled: bookingsByStatus.CANCELLED,
    totalBookingsCompleted: bookingsByStatus.COMPLETED,
    totalRevenue,
    bookingsByStatus,
    topPackages,
    packageOccupancy,
    recentBookings,
    conversionRate,
    alerts,
  };
};
