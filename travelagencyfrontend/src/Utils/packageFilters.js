// Define un conjunto de filtros vacíos para la búsqueda de paquetes turísticos.
export const EMPTY_PACKAGE_FILTERS = {
  destination: '',
  startDate: '',
  endDate: '',
  durationDays: '',
  availableSlots: '',
  minPrice: '',
  maxPrice: '',
  travelType: '',
};
// Crea los parámetros de búsqueda a partir de los filtros.
export const buildPackageSearchParams = (searchParams) => {
  const params = {};
  // Crea los parámetros de búsqueda a partir de los filtros.
  if (searchParams.destination?.trim()) {
    params.destination = searchParams.destination.trim();
  }
  if (searchParams.startDate) {
    params.startDate = searchParams.startDate;
  }
  if (searchParams.endDate) {
    params.endDate = searchParams.endDate;
  }
  if (searchParams.durationDays) {
    params.minDurationDays = Number(searchParams.durationDays);
  }
  if (searchParams.availableSlots) {
    params.minAvailableSlots = Number(searchParams.availableSlots);
  }
  if (searchParams.minPrice) {
    params.minPrice = Number(searchParams.minPrice);
  }
  if (searchParams.maxPrice) {
    params.maxPrice = Number(searchParams.maxPrice);
  }
  if (searchParams.travelType?.trim()) {
    params.travelType = searchParams.travelType.trim();
  }

  return params;
};

// Filtra los paquetes localmente según los filtros.
export const filterPackagesLocally = (sourcePackages, searchParams) => {
  // Filtra los paquetes localmente según los filtros.
  return sourcePackages.filter((pkg) => {
    // Filtra los paquetes localmente según los filtros.
    const destinationFilter = searchParams.destination.trim().toLowerCase();
    const travelTypeFilter = searchParams.travelType.trim().toLowerCase();
    const minPrice = searchParams.minPrice ? Number(searchParams.minPrice) : null;
    const maxPrice = searchParams.maxPrice ? Number(searchParams.maxPrice) : null;
    // Si el filtro de destino no coincide con el paquete, devuelve falso.
    if (destinationFilter && !pkg.destination?.toLowerCase().includes(destinationFilter)) {
      return false;
    }

    if (travelTypeFilter && !pkg.travelType?.toLowerCase().includes(travelTypeFilter)) {
      return false;
    }

    if (searchParams.startDate && new Date(pkg.startDate) < new Date(searchParams.startDate)) {
      return false;
    }

    if (searchParams.endDate && new Date(pkg.endDate) > new Date(searchParams.endDate)) {
      return false;
    }

    if (
      searchParams.availableSlots &&
      Number(pkg.availableSlots) < Number(searchParams.availableSlots)
    ) {
      return false;
    }

    if (
      searchParams.durationDays &&
      Number(pkg.durationDays) < Number(searchParams.durationDays)
    ) {
      return false;
    }

    if (minPrice !== null && Number(pkg.price) < minPrice) {
      return false;
    }

    if (maxPrice !== null && Number(pkg.price) > maxPrice) {
      return false;
    }

    return true;
  });
};

// Obtiene el número de cupos disponibles del paquete.
export const getAvailableSlots = (pkg) => Number(pkg?.availableSlots ?? pkg?.totalSlots ?? 0);