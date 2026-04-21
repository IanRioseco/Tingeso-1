import { useEffect, useState } from 'react';
import PackageCard from '../../Components/PackageCard/packageCard.jsx';
import PackageService from '../../Services/packageService';
import './package.css';

const EMPTY_FILTERS = {
  destination: '',
  startDate: '',
  endDate: '',
  durationDays: '',
  availableSlots: '',
  price: '',
};

export default function PackagesPage() {
  const [allPackages, setAllPackages] = useState([]);
  const [packages, setPackages] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchParams, setSearchParams] = useState(EMPTY_FILTERS);

  const fetchAvailablePackages = async () => {
    const response = await PackageService.getAvailable();
    return response.data;
  };

  useEffect(() => {
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

  const handleSearch = async (e) => {
    e.preventDefault();

    try {
      setLoading(true);
      setError(null);

      // Workaround temporal: /search del backend devuelve 500 con nulls.
      // Filtramos en frontend a partir de /available para mantener la pagina usable.
      const sourcePackages = allPackages.length > 0 ? allPackages : await fetchAvailablePackages();
      if (allPackages.length === 0) {
        setAllPackages(sourcePackages);
      }

      const filteredPackages = sourcePackages.filter((pkg) => {
        const destinationFilter = searchParams.destination.trim().toLowerCase();
        if (destinationFilter && !pkg.destination?.toLowerCase().includes(destinationFilter)) {
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

        if (searchParams.price && Number(pkg.price) > Number(searchParams.price)) {
          return false;
        }

        return true;
      });

      setPackages(filteredPackages);
    } catch (err) {
      setError('Error al buscar paquetes');
    } finally {
      setLoading(false);
    }
  };

  const handleReset = async () => {
    setSearchParams(EMPTY_FILTERS);

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

  const handleChange = (e) => {
    setSearchParams({ ...searchParams, [e.target.name]: e.target.value });
  };

  return (
    <div className="packages-page container">
      <h1>Paquetes de viaje</h1>

      <form className="packages-search" onSubmit={handleSearch}>
        <div className="form-group">
          <label htmlFor="destination">Destino:</label>
          <input
            type="text"
            id="destination"
            name="destination"
            value={searchParams.destination}
            onChange={handleChange}
            placeholder="Ej: Santiago"
          />
        </div>

        <div className="form-group">
          <label htmlFor="startDate">Fecha de inicio:</label>
          <input
            type="date"
            id="startDate"
            name="startDate"
            value={searchParams.startDate}
            onChange={handleChange}
          />
        </div>

        <div className="form-group">
          <label htmlFor="endDate">Fecha de fin:</label>
          <input
            type="date"
            id="endDate"
            name="endDate"
            value={searchParams.endDate}
            onChange={handleChange}
          />
        </div>

        <div className="form-group">
          <label htmlFor="durationDays">Duracion (dias):</label>
          <input
            type="number"
            id="durationDays"
            name="durationDays"
            min="1"
            value={searchParams.durationDays}
            onChange={handleChange}
            placeholder="Ej: 7"
          />
        </div>

        <div className="form-group">
          <label htmlFor="availableSlots">Cupos minimos:</label>
          <input
            type="number"
            id="availableSlots"
            name="availableSlots"
            min="1"
            value={searchParams.availableSlots}
            onChange={handleChange}
            placeholder="Ej: 4"
          />
        </div>

        <div className="form-group">
          <label htmlFor="price">Precio maximo:</label>
          <input
            type="number"
            id="price"
            name="price"
            min="0"
            value={searchParams.price}
            onChange={handleChange}
            placeholder="Ej: 250000"
          />
        </div>

        <div className="packages-search-actions">
          <button type="submit" className="btn btn-primary">Buscar</button>
          <button type="button" className="btn btn-secondary" onClick={handleReset}>Limpiar</button>
        </div>
      </form>

      {loading && <p>Cargando paquetes...</p>}
      {error && <p>{error}</p>}
      {!loading && !error && <p>Resultados: {packages.length}</p>}
      {!loading && !error && packages.length === 0 && (
        <p>No se encontraron paquetes con esos filtros.</p>
      )}

      <div className="packages-list">
        {packages.map((pkg) => (
          <PackageCard key={pkg.id} pkg={pkg} />
        ))}
      </div>
    </div>
  );
}
