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

const MyBookingsPage = () => <h2>Mis reservas</h2>;
const AdminDashboardPage = () => <h2>Panel admin</h2>;

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
            path="/my-bookings"
            element={(
              <PrivateRoute roles={['USER', 'ADMIN']}>
                <MyBookingsPage />
              </PrivateRoute>
            )}
          />
          <Route
            path="/profile"
            element={(
              <PrivateRoute roles={['USER', 'ADMIN']}>
                <ProfilePage />
              </PrivateRoute>
            )}
          />
          <Route
            path="/admin"
            element={(
              <PrivateRoute roles={['ADMIN']}>
                <AdminDashboardPage />
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
