import {useState, useEffect, useCallback} from 'react';
import { useNavigate } from 'react-router-dom';
import { getDashboardStats } from '../../../Services/dashboardService';
import { formatPesos } from '../../../Utils/currency';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import {
    faUsers,
    faSuitcaseRolling,
    faCalendarCheck,
    faCalendarXmark,
    faClipboardList,
    faClipboardCheck,
    faDollarSign,
    faExclamationTriangle,
    faClock,
    faChartPie,
    faChartLine,
    faRankingStar,
    faLayerGroup,
    faRotateRight,
} from '@fortawesome/free-solid-svg-icons';
import './dashboard.css';


export default function Dashboard() {
    const  navigate = useNavigate();
    const [stats, setStats] = useState({
        totalUsers: 0,
        totalPackages: 0,
        totalBookings: 0,
        totalBookingsCancelled: 0,
        totalBookingsCompleted: 0,
        totalRevenue: 0,
        bookingsByStatus: {},
        topPackages: [],
        packageOccupancy: [],
        recentBookings: [],
        conversionRate: 0,
        alerts: [],
    });

    const [loading, setLoading] = useState(true);
    const [refreshing, setRefreshing] = useState(false);

    const loadStats = useCallback(async ({ refresh = false } = {}) => {
        if (refresh) {
            setRefreshing(true);
        } else {
            setLoading(true);
        }

        try {
            const dashboardStats = await getDashboardStats();
            setStats({
                ...dashboardStats,
                alerts: dashboardStats.alerts.map((alert) => ({
                    ...alert,
                    icon: alert.type === 'warning' ? faExclamationTriangle : faClock,
                })),
            });
        } catch (err) {
            console.error('Error loading dashboard:', err);
        } finally {
            setLoading(false);
            setRefreshing(false);
        }
    }, []);

    const handleRefresh = () => loadStats({ refresh: true });

    useEffect(() => {
        loadStats();
    }, [loadStats]);

    const formatDate = (dateString) => {
        return new Date(dateString).toLocaleDateString('es-CL');
    };

    const sectionTitle = (icon, text) => (
        <span className="dashboard-section-title">
            <FontAwesomeIcon icon={icon} className="dashboard-section-title-icon fa-icon-app fa-icon-primary" />
            <span>{text}</span>
        </span>
    );

    return (
        <div className="admin-dashboard container">
            <div className="admin-dashboard-header">
                <div>
                    <h1>Panel de Control</h1>
                    <p className='admin-dashboard-subtitle'>Bienvenido al panel de control administrativo</p>
                </div>
                <button 
                    className="btn-refresh" 
                    onClick={handleRefresh}
                    disabled={refreshing}
                    title="Actualizar datos"
                >
                    <FontAwesomeIcon 
                        icon={faRotateRight} 
                        className={refreshing ? 'fa-spin' : ''}
                    />
                    {refreshing ? 'Actualizando...' : 'Actualizar'}
                </button>
            </div>

            {/* CARGADOR DE DATOS */}
            {loading ? (
                <p className='admin-dashboard-loading'>Cargando datos...</p>
            ) : (
                <>
                    {/* ALERTAS */}
                    {stats.alerts.length > 0 && (
                        <div className='dashboard-alerts'>
                            <h3>
                                <FontAwesomeIcon icon={faExclamationTriangle} className="dashboard-heading-icon fa-icon-app fa-icon-warning" />
                                <span>Alertas Importantes</span>
                            </h3>
                            {stats.alerts.map((alert, idx) => (
                                <div key={idx} className={`alert alert-${alert.type}`}>
                                    <FontAwesomeIcon
                                        icon={alert.icon}
                                        className={`alert-icon fa-icon-app ${alert.type === 'warning' ? 'fa-icon-warning' : 'fa-icon-accent'}`}
                                    />
                                    <span>{alert.message}</span>
                                </div>
                            ))}
                        </div>
                    )}

                    {/* TARJETAS PRINCIPALES */}
                    <div className='admin-dashboard-cards'>
                        <div className='admin-dashboard-card' onClick={() => navigate('/admin/users')}>
                            <span><FontAwesomeIcon icon={faUsers} className='dashboard-card-icon fa-icon-app fa-icon-primary' /></span>
                            <h3>Usuarios Registrados</h3>
                            <p>{stats.totalUsers}</p>
                        </div>
                        <div className='admin-dashboard-card' onClick={() => navigate('/admin/packages')}>
                            <span><FontAwesomeIcon icon={faSuitcaseRolling} className='dashboard-card-icon fa-icon-app fa-icon-accent' /></span>
                            <h3>Paquetes Disponibles</h3>
                            <p>{stats.totalPackages}</p>
                        </div>
                        <div className='admin-dashboard-card' onClick={() => navigate('/admin/bookings')}>
                            <span><FontAwesomeIcon icon={faCalendarCheck} className='dashboard-card-icon fa-icon-app fa-icon-cta' /></span>
                            <h3>Reservas Realizadas</h3>
                            <p>{stats.totalBookings}</p>
                        </div>
                        <div className='admin-dashboard-card highlight'>
                            <span><FontAwesomeIcon icon={faDollarSign} className='dashboard-card-icon fa-icon-app fa-icon-warm' /></span>
                            <h3>Ingresos Totales</h3>
                            <p className='revenue-text'>{formatPesos(stats.totalRevenue)}</p>
                        </div>
                    </div>

                    {/* DESGLOSE DE RESERVAS POR ESTADO */}
                    <div className='dashboard-section'>
                        <h3>{sectionTitle(faChartPie, 'Desglose de Reservas por Estado')}</h3>
                        <div className='status-breakdown'>
                            <div className='status-item pending'>
                                <FontAwesomeIcon icon={faClock} className='status-item-icon fa-icon-app fa-icon-warm' />
                                <p className='status-label'>Pendientes</p>
                                <p className='status-value'>{stats.bookingsByStatus.PENDING || 0}</p>
                            </div>
                            <div className='status-item confirmed'>
                                <FontAwesomeIcon icon={faCalendarCheck} className='status-item-icon fa-icon-app fa-icon-primary' />
                                <p className='status-label'>Confirmadas</p>
                                <p className='status-value'>{stats.bookingsByStatus.CONFIRMED || 0}</p>
                            </div>
                            <div className='status-item completed'>
                                <FontAwesomeIcon icon={faClipboardCheck} className='status-item-icon fa-icon-app fa-icon-success' />
                                <p className='status-label'>Completadas</p>
                                <p className='status-value'>{stats.bookingsByStatus.COMPLETED || 0}</p>
                            </div>
                            <div className='status-item cancelled'>
                                <FontAwesomeIcon icon={faCalendarXmark} className='status-item-icon fa-icon-app fa-icon-danger' />
                                <p className='status-label'>Canceladas</p>
                                <p className='status-value'>{stats.bookingsByStatus.CANCELLED || 0}</p>
                            </div>
                        </div>
                    </div>

                    {/* MÉTRICAS CLAVE */}
                    <div className='dashboard-section'>
                        <h3>{sectionTitle(faChartLine, 'Métricas Clave')}</h3>
                        <div className='metrics-grid'>
                            <div className='metric-card'>
                                <p className='metric-label'>Tasa de Conversión</p>
                                <p className='metric-value'>{stats.conversionRate.toFixed(1)}%</p>
                                <div className='metric-bar'>
                                    <div 
                                        className='metric-bar-fill'
                                        style={{width: `${stats.conversionRate}%`}}
                                    ></div>
                                </div>
                            </div>
                            <div className='metric-card'>
                                <p className='metric-label'>Promedio por Reserva</p>
                                <p className='metric-value'>
                                    {stats.totalBookings > 0 ? formatPesos(stats.totalRevenue / stats.totalBookings) : '$0'}
                                </p>
                            </div>
                        </div>
                    </div>

                    {/* PAQUETES MÁS POPULARES */}
                    {stats.topPackages.length > 0 && (
                        <div className='dashboard-section'>
                            <h3>{sectionTitle(faRankingStar, 'Paquetes Más Populares')}</h3>
                            <div className='top-packages'>
                                {stats.topPackages.map((pkg, idx) => (
                                    <div key={pkg.id} className='top-package-item'>
                                        <div className='rank'>#{idx + 1}</div>
                                        <div className='pkg-info'>
                                            <h4>{pkg.name}</h4>
                                            <p className='pkg-destination'>{pkg.destination}</p>
                                        </div>
                                        <div className='pkg-stats'>
                                            <span className='reservations'>
                                                📅 {pkg.reservationCount} reservas
                                            </span>
                                            <span className='revenue'>
                                                💰 {formatPesos(pkg.revenue)}
                                            </span>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </div>
                    )}

                    {/* OCUPACIÓN DE PAQUETES */}
                    {stats.packageOccupancy.length > 0 && (
                        <div className='dashboard-section'>
                            <h3>{sectionTitle(faLayerGroup, 'Ocupación de Paquetes')}</h3>
                            <div className='occupancy-list'>
                                {stats.packageOccupancy.map(occ => (
                                    <div key={occ.id} className='occupancy-item'>
                                        <div className='occupancy-header'>
                                            <span className='pkg-name'>{occ.name}</span>
                                            <span className='occupancy-text'>
                                                {occ.reserved}/{occ.total} cupos
                                            </span>
                                        </div>
                                        <div className='occupancy-bar'>
                                            <div 
                                                className={`occupancy-bar-fill ${
                                                    occ.percentage >= 90 ? 'critical' : 
                                                    occ.percentage >= 70 ? 'warning' : 'normal'
                                                }`}
                                                style={{width: `${occ.percentage}%`}}
                                            ></div>
                                        </div>
                                        <span className='occupancy-percentage'>
                                            {occ.percentage.toFixed(0)}%
                                        </span>
                                    </div>
                                ))}
                            </div>
                        </div>
                    )}

                    {/* ACTIVIDAD RECIENTE */}
                    {stats.recentBookings.length > 0 && (
                        <div className='dashboard-section'>
                            <h3>{sectionTitle(faClipboardList, 'Últimas Reservas')}</h3>
                            <div className='recent-bookings'>
                                <table className='bookings-table'>
                                    <thead>
                                        <tr>
                                            <th>ID</th>
                                            <th>Paquete</th>
                                            <th>Pasajeros</th>
                                            <th>Total</th>
                                            <th>Estado</th>
                                            <th>Fecha</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {stats.recentBookings.map(booking => (
                                            <tr key={booking.id}>
                                                <td>#{booking.id}</td>
                                                <td>{booking.packageName}</td>
                                                <td>{booking.passengers ?? booking.numberOfPassengers ?? 1}</td>
                                                <td>{formatPesos(booking.finalAmount ?? booking.totalPrice ?? 0)}</td>
                                                <td>
                                                    <span className={`status-badge ${booking.bookingStatus.toLowerCase()}`}>
                                                        {booking.bookingStatus}
                                                    </span>
                                                </td>
                                                <td>{formatDate(booking.createdAt)}</td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    )}
                </>
            )}
        </div>
    );
}