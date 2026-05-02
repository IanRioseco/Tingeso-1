import { useEffect, useState } from 'react';
import PackageCard from '../../Components/PackageCard/packageCard.jsx';
import PackageService from '../../Services/packageService';
import { EMPTY_PACKAGE_FILTERS, buildPackageSearchParams, filterPackagesLocally } from '../../Utils/packageFilters';
import './package.css';

export default function PackagesPage() {
  const [allPackages, setAllPackages] = useState([]);
  const [packages, setPackages] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchParams, setSearchParams] = useState(EMPTY_PACKAGE_FILTERS);
  const [showFilters, setShowFilters] = useState(false);

  const fetchAvailablePackages = async () => {
    const response = await PackageService.getAvailable();
    return response.data;
  };

  useEffect(() => {
    // Carga los paquetes disponibles.
    const loadPackages = async () => {
      try {
        setLoading(true);
        setError(null);
        const availablePackages = await fetchAvailablePackages();
        setAllPackages(availablePackages);
        setPackages(availablePackages);
      } catch (err) {
        setError('Error al cargar los paquetes');
      } finally {
        setLoading(false);
      }
    };

    loadPackages();
  }, []);
  // Realiza la búsqueda de paquetes y carga los paquetes filtrados.
  const handleSearch = async (e) => {
    e.preventDefault();
    // Crea los parámetros de búsqueda a partir de los filtros.
    const params = buildPackageSearchParams(searchParams);

    try {
      setLoading(true);
      setError(null);
      // Realiza la búsqueda de paquetes.
      const response = await PackageService.search(params);
      const searchResult = response.data ?? [];

      if (searchResult.length > 0 || Object.keys(params).length > 0) {
        setPackages(searchResult);
        if (allPackages.length === 0) {
          setAllPackages(searchResult);
        }
        return;
      }
      // Si no hay resultados, carga los paquetes disponibles.
      const sourcePackages = allPackages.length > 0 ? allPackages : await fetchAvailablePackages();
      if (allPackages.length === 0) {
        setAllPackages(sourcePackages);
      }
      setPackages(filterPackagesLocally(sourcePackages, searchParams));
    } catch (err) {
      try {
        const sourcePackages = allPackages.length > 0 ? allPackages : await fetchAvailablePackages();
        if (allPackages.length === 0) {
          setAllPackages(sourcePackages);
        }
        setPackages(filterPackagesLocally(sourcePackages, searchParams));
        setError(null);
      } catch (fallbackErr) {
        setError('Error al buscar paquetes');
      }
    } finally {
      setLoading(false);
    }
  };
  // Limpia los filtros de búsqueda y carga los paquetes disponibles.
  const handleReset = async () => {
    setSearchParams(EMPTY_PACKAGE_FILTERS);

    try {
      setLoading(true);
      setError(null);
      const availablePackages = allPackages.length > 0
        ? allPackages
        : await fetchAvailablePackages();
      if (allPackages.length === 0) {
        setAllPackages(availablePackages);
      }
      setPackages(availablePackages);
    } catch (err) {
      setError('Error al cargar los paquetes');
    } finally {
      setLoading(false);
    }
  };
  // Actualiza los filtros de búsqueda y carga los paquetes filtrados.
  const handleChange = (e) => {
    setSearchParams({ ...searchParams, [e.target.name]: e.target.value });
  };

  return (
    <div className="packages-page container">
      <div className="packages-header">
        <h1>Paquetes de Viaje</h1>
        <p className="packages-subtitle">Descubre tu próxima gran aventura</p>
        <button 
          className="btn btn-outline toggle-filters-btn" 
          onClick={() => setShowFilters(!showFilters)}
        >
          {showFilters ? 'Ocultar Filtros ∧' : 'Filtrar Búsqueda ∨'}
        </button>
      </div>
      
      {/* Formulario de búsqueda de paquetes */}
      {showFilters && (
      <div className="packages-search-container">
        <form className="packages-search" onSubmit={handleSearch}>
          {/* Fila 1 principal: Destino y Fechas */}
          <div className="search-row-main">
            {/* Campo para el destino o palabra clave */}
            <div className="form-group main-search">
              <label htmlFor="destination">Destino o palabra clave</label>
              <div className="input-with-icon">
                <span className="search-icon">🔍</span>
                <input type="text" id="destination" name="destination" value={searchParams.destination} onChange={handleChange} placeholder="Ej: Santiago, Playa, Montaña..." />
              </div>
            </div>
            {/* Campo para la fecha de inicio del paquete */}
            <div className="form-group">
              <label htmlFor="startDate">Fecha Ida</label>
              <input type="date" id="startDate" name="startDate" value={searchParams.startDate} onChange={handleChange} />
            </div>
            {/* Campo para la fecha de finalización del paquete */}
            <div className="form-group">
              <label htmlFor="endDate">Fecha Vuelta</label>
              <input type="date" id="endDate" name="endDate" value={searchParams.endDate} onChange={handleChange} />
            </div>
          </div>

          {/* Fila 2 filtros secundarios: Detalles y Precios */}
          <div className="search-row-secondary">
            <div className="form-group">
              <label htmlFor="travelType">Tipo de experiencia</label>
              <input type="text" id="travelType" name="travelType" value={searchParams.travelType} onChange={handleChange} placeholder="Aventura, Relax, Cultural..." />
            </div>
            {/* Campo para el número de días mínimos del paquete */}
            <div className="form-group">
              <label htmlFor="durationDays">Días mín.</label>
              <input type="number" id="durationDays" name="durationDays" min="1" value={searchParams.durationDays} onChange={handleChange} placeholder="Ej: 3" />
            </div>
            {/* Campo para el número de cupos disponibles del paquete */}
            <div className="form-group">
              <label htmlFor="availableSlots">Cupos mín.</label>
              <input type="number" id="availableSlots" name="availableSlots" min="1" value={searchParams.availableSlots} onChange={handleChange} placeholder="Ej: 2" /> 
            </div>
            {/* Campo para el precio mínimo del paquete */}
            <div className="form-group">
              <label htmlFor="minPrice">Precio mínimo ($)</label>
              <input type="number" id="minPrice" name="minPrice" min="0" value={searchParams.minPrice} onChange={handleChange} placeholder="Ej: 50000" />
            </div>
            {/* Campo para el precio máximo del paquete */}
            <div className="form-group">
              <label htmlFor="maxPrice">Precio máximo ($)</label>
              <input type="number" id="maxPrice" name="maxPrice" min="0" value={searchParams.maxPrice} onChange={handleChange} placeholder="Ej: 500000" />
            </div>
          </div>
          {/* Botón de búsqueda y botón de limpieza de filtros */}
          <div className="packages-search-actions">
            <button type="submit" className="btn btn-primary search-btn">
              Buscar Paquetes
            </button>
            <button type="button" className="btn btn-secondary reset-btn" onClick={handleReset}>
              Limpiar Filtros
            </button>
          </div>
        </form>
      </div>
      )}
      {/* Encabezado de resultados de búsqueda */}
      <div className="packages-results-header">
        {loading && <p className="loading-text">Cargando increíbles paquetes...</p>}
        {error && <p className="error-text">{error}</p>}
        {!loading && !error && (
          <h2 className="results-count">
            {packages.length} {packages.length === 1 ? 'Paquete encontrado' : 'Paquetes encontrados'}
          </h2>
        )}
      </div>
      {/* Si no hay resultados, mostramos un mensaje y un botón para volver a la página de paquetes */}
      {!loading && !error && packages.length === 0 && (
        <div className="no-results">
          <p>No logramos encontrar paquetes que coincidan con tu búsqueda.</p>
          <button className="btn btn-outline" onClick={handleReset}>Ver todos los paquetes</button>
        </div>
      )}
      {/* Lista de paquetes filtrados */}
      <div className="packages-list">
        {packages.map((pkg) => (
          <PackageCard key={pkg.id} pkg={pkg} />
        ))}
      </div>
    </div>
  );
}
