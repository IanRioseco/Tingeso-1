import packageService from './packageService';
import { buildPackageSearchParams, filterPackagesLocally } from '../Utils/packageFilters';

// Carga los paquetes disponibles.
const fetchAvailablePackages = async () => {
  const response = await packageService.getAvailable();
  return response.data ?? [];
};

// Carga los paquetes disponibles y los paquetes filtrados.
export const loadInitialPackages = async () => {
  const availablePackages = await fetchAvailablePackages();
  // Devuelve los paquetes disponibles y los paquetes filtrados.
  return {
    packages: availablePackages,
    allPackages: availablePackages,
  };
};

// Realiza la búsqueda de paquetes y carga los paquetes filtrados.
export const searchPackagesWithFallback = async (searchParams, currentAllPackages = []) => {
  const params = buildPackageSearchParams(searchParams);
  // Crea los parámetros de búsqueda a partir de los filtros.
  try {
    // Realiza la búsqueda de paquetes.
    const response = await packageService.search(params);
    const searchResult = response.data ?? [];
    // Si hay resultados o parámetros de búsqueda, devuelve los paquetes filtrados.
    if (searchResult.length > 0 || Object.keys(params).length > 0) {
      return {
        packages: searchResult,
        allPackages: currentAllPackages.length > 0 ? currentAllPackages : searchResult,
      };
    }
  } catch {
    // Si falla la búsqueda remota, se utiliza fallback local.
  }

  // Si no hay resultados, carga los paquetes disponibles.
  const sourcePackages = currentAllPackages.length > 0
    ? currentAllPackages
    : await fetchAvailablePackages();
  // Devuelve los paquetes filtrados.
  return {
    packages: filterPackagesLocally(sourcePackages, searchParams),
    allPackages: sourcePackages,
  };
};

// Restablece los paquetes a los paquetes disponibles.
export const resetPackagesToAvailable = async (currentAllPackages = []) => {
  const sourcePackages = currentAllPackages.length > 0
    ? currentAllPackages
    : await fetchAvailablePackages();
  // Devuelve los paquetes filtrados.
  return {
    packages: sourcePackages,
    allPackages: sourcePackages,
  };
};
