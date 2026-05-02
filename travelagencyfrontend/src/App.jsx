import './App.css';
import { useState } from 'react';
import { Navigate, Route, Routes } from 'react-router-dom';
import Navbar from './Components/Navbar/navbar.jsx';
import Footer from './Components/Footer/footer.jsx';
import PrivateRoute from './Components/ProtectedRoute/PrivateRoute.jsx';
import LoginPage from './Pages/Login/login.jsx';
import RegisterPage from './Pages/Register/register.jsx';
import PackagesPage from './Pages/Packages/package.jsx';
import PackageDetailPage from './Pages/PackageDetail/packagedetail.jsx';
import ProfilePage from './Pages/Profile/profile.jsx';
import HomePage from './Pages/Home/home.jsx';
import ManagePackagePage from './Pages/Admin/ManagePackage/managepackage.jsx';
import AdminDashboardPage from './Pages/Admin/Dashboard/dashboard.jsx';
import BookingPage from './Pages/Booking/booking.jsx';
import MyBookingsPage from './Pages/MyBookings/mybookings.jsx';
import PromotionManagementPage from './Pages/Admin/ManagePromotions/managepromotions.jsx';
import ManageUsersPage from './Pages/Admin/ManageUsers/manageusers.jsx';
import PaymentPage from './Pages/Payment/payment.jsx';
import ReportsPage from './Pages/Admin/Reports/reports.jsx';
import AdminBookingsPage from './Pages/Admin/Bookings/bookings.jsx';

function App() {
  const [sidebarOpen, setSidebarOpen] = useState(false);

  return (
    <div className={`app-shell ${sidebarOpen ? 'sidebar-expanded' : ''}`}>
      <Navbar onSidebarStateChange={setSidebarOpen} />
      <main className="app-container">
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/packages" element={<PackagesPage />} />
          <Route path="/packages/:id" element={<PackageDetailPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route
            path="/booking/:id"
            element={(
              <PrivateRoute roles={['USER', 'ADMIN']}>
                <BookingPage />
              </PrivateRoute>
            )}
          />
          <Route
            path="/payment/:bookingId"
            element={(
              <PrivateRoute roles={['USER', 'ADMIN']}>
                <PaymentPage />
              </PrivateRoute>
            )}
          />
          {/* Rutas protegidas para usuarios autenticados */}
          <Route
            path="/my-bookings"
            element={(
              <PrivateRoute roles={['USER', 'ADMIN']}>
                <MyBookingsPage />
              </PrivateRoute>
            )}
          />
          {/* Rutas privadas para administradores */}
          <Route
            path="/profile"
            element={(
              <PrivateRoute roles={['USER', 'ADMIN']}>
                <ProfilePage />
              </PrivateRoute>
            )}
          />
          {/* Rutas privadas para administradores */}
          <Route
            path="/admin"
            element={(
              <PrivateRoute roles={['ADMIN']}>
                <AdminDashboardPage />
              </PrivateRoute>
            )}
          />
          <Route
            path="/admin/bookings"
            element={(
              <PrivateRoute roles={['ADMIN']}>
                <AdminBookingsPage />
              </PrivateRoute>
            )}
          />
          {/* Rutas privadas para administradores */}
          <Route
            path="/admin/users"
            element={(
              <PrivateRoute roles={['ADMIN']}>
                <ManageUsersPage />
              </PrivateRoute>
            )}
          />
          {/* Rutas privadas para administradores */}
          <Route
            path="/Admin/ManagePackage"
            element={(
              <PrivateRoute roles={['ADMIN']}>
                <ManagePackagePage />
              </PrivateRoute>
            )}
          />
          <Route
            path="/admin/packages"
            element={(
              <PrivateRoute roles={['ADMIN']}>
                <ManagePackagePage />
              </PrivateRoute>
            )}
          />
          {/* Rutas privadas para administradores */}
          <Route
            path="/Admin/ManagePromotions"
            element={(
              <PrivateRoute roles={['ADMIN']}>
                <PromotionManagementPage />
              </PrivateRoute>
            )}
          />
          <Route
            path="/admin/promotions"
            element={(
              <PrivateRoute roles={['ADMIN']}>
                <PromotionManagementPage />
              </PrivateRoute>
            )}
          />
          <Route
            path="/admin/reports"
            element={(
              <PrivateRoute roles={['ADMIN']}>
                <ReportsPage />
              </PrivateRoute>
            )}
          />

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
      <Footer />
    </div>
  );
}

export default App;
