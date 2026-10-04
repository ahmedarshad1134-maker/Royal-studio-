import React, { useState } from 'react';
import { AppProvider, useApp } from './context/AppContext';
import { Navbar } from './components/Navbar';
import { Footer } from './components/Footer';
import { NotificationModal } from './components/NotificationModal';
import { HomeScreen } from './screens/HomeScreen';
import { PortfolioScreen } from './screens/PortfolioScreen';
import { ServicesScreen } from './screens/ServicesScreen';
import { ServiceDetailScreen } from './screens/ServiceDetailScreen';
import { PackagesScreen } from './screens/PackagesScreen';
import { PackageDetailScreen } from './screens/PackageDetailScreen';
import { BookingScreen } from './screens/BookingScreen';
import { CustomerAreaScreen } from './screens/CustomerAreaScreen';
import { CustomerPrivateGalleryScreen } from './screens/CustomerPrivateGalleryScreen';
import { ReviewsScreen } from './screens/ReviewsScreen';
import { AboutScreen } from './screens/AboutScreen';
import { ContactScreen } from './screens/ContactScreen';
import { AdminScreen } from './screens/AdminScreen';

const MainApp: React.FC = () => {
  const { currentScreen } = useApp();
  const [notificationsOpen, setNotificationsOpen] = useState(false);

  const renderScreen = () => {
    switch (currentScreen) {
      case 'home':
        return <HomeScreen />;
      case 'portfolio':
        return <PortfolioScreen />;
      case 'services':
        return <ServicesScreen />;
      case 'service_detail':
        return <ServiceDetailScreen />;
      case 'packages':
        return <PackagesScreen />;
      case 'package_detail':
        return <PackageDetailScreen />;
      case 'booking':
        return <BookingScreen />;
      case 'customer_area':
        return <CustomerAreaScreen />;
      case 'private_gallery':
        return <CustomerPrivateGalleryScreen />;
      case 'reviews':
        return <ReviewsScreen />;
      case 'about':
        return <AboutScreen />;
      case 'contact':
        return <ContactScreen />;
      case 'admin':
        return <AdminScreen />;
      default:
        return <HomeScreen />;
    }
  };

  return (
    <div className="min-h-screen flex flex-col bg-[#0A0A0B] text-[#EBEBEB] selection:bg-[#D4AF37]/30 selection:text-[#D4AF37]">
      <Navbar onOpenNotifications={() => setNotificationsOpen(true)} />
      <main className="flex-1">{renderScreen()}</main>
      <Footer />
      <NotificationModal
        isOpen={notificationsOpen}
        onClose={() => setNotificationsOpen(false)}
      />
    </div>
  );
};

export const App: React.FC = () => {
  return (
    <AppProvider>
      <MainApp />
    </AppProvider>
  );
};

export default App;
