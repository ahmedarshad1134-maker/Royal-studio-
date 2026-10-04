export type UserRole = 'customer' | 'admin';

export interface User {
  uid: string;
  name: string;
  phone: string;
  email: string;
  role: UserRole;
  createdAt?: string;
  updatedAt?: string;
}

export type BookingStatus =
  | 'NEW'
  | 'CONTACTED'
  | 'QUOTATION_SENT'
  | 'CONFIRMED'
  | 'COMPLETED'
  | 'CANCELLED';

export interface AuditEntry {
  status: string;
  updatedBy: string;
  timestamp: number;
  note: string;
  adminOnly?: boolean;
}

export interface Booking {
  id: string;
  referenceId: string;
  customerId: string;
  customerName: string;
  phone: string;
  email: string;
  eventType: string;
  eventDate: number; // timestamp in ms
  eventLocation: string;
  serviceId?: string;
  packageId?: string;
  message?: string;
  status: BookingStatus;
  createdAt: number;
  updatedAt?: number;
  adminNotes?: string;
  updatedBy?: string;
  auditTrail: AuditEntry[];
}

export interface BlockedDate {
  id: string;
  date: number; // timestamp in ms
  reason: string;
  createdAt?: number;
}

export interface ServiceItem {
  id: string;
  name: string;
  shortDescription: string;
  description: string;
  imageUrl: string;
  features: string[];
  startingPrice: string;
  suitableEventTypes: string[];
  enabled: boolean;
}

export interface PackageItem {
  id: string;
  name: string;
  description: string;
  price: string;
  currency: string;
  includedServices: string[];
  photographyHours: string;
  videographyHours: string;
  numberOfPhotographers: number;
  numberOfVideographers: number;
  albumInformation: string;
  deliveryInformation: string;
  optionalAddOns: string[];
  isRecommended: boolean;
  enabled: boolean;
}

export type PortfolioCategory =
  | 'ALL'
  | 'WEDDING'
  | 'ENGAGEMENT'
  | 'PRE_WEDDING'
  | 'BIRTHDAY'
  | 'BABY_FAMILY'
  | 'ANNIVERSARY'
  | 'OTHER_EVENTS';

export type MediaType = 'IMAGE' | 'VIDEO';

export interface PortfolioItem {
  id: string;
  title: string;
  category: PortfolioCategory;
  type: MediaType;
  imageUrl: string;
  thumbnailUrl?: string;
  featured: boolean;
  enabled: boolean;
  createdAt: number;
}

export interface ReviewItem {
  id: string;
  customerId?: string;
  customerName: string;
  rating: number; // 1-5
  eventType: string;
  review: string;
  photoUrl?: string;
  approved: boolean;
  featured: boolean;
  createdAt: number;
}

export interface LineItem {
  description: string;
  amount: number;
  category: 'PACKAGE' | 'SERVICE' | 'EXTRA' | 'DISCOUNT';
}

export interface PaymentRecord {
  paymentId: string;
  amount: number;
  method: 'Bank Transfer' | 'Cash' | 'Card' | 'UPI' | 'Cheque';
  referenceNotes: string;
  recordedBy: string;
  timestamp: number;
  receiptId: string;
}

export type PaymentStatus = 'Unpaid' | 'Partially Paid' | 'Paid' | 'Refunded';

export interface Invoice {
  id: string;
  invoiceNumber: string;
  bookingId: string;
  bookingReference: string;
  customerId: string;
  customerName: string;
  customerPhone: string;
  customerEmail: string;
  eventType: string;
  eventDate: number;
  eventLocation: string;
  selectedPackage: string;
  selectedServices: string[];
  lineItems: LineItem[];
  subtotal: number;
  additionalCharges: number;
  discount: number;
  taxRate: number;
  taxAmount: number;
  totalAmount: number;
  amountPaid: number;
  balanceDue: number;
  paymentStatus: PaymentStatus;
  currency: string;
  issueDate: number;
  dueDate: number;
  paymentRecords: PaymentRecord[];
  notes: string;
  createdAt: number;
  updatedAt?: number;
}

export interface StudioSettings {
  studioName: string;
  tagline: string;
  phone: string;
  whatsapp: string;
  email: string;
  address: string;
  businessHours: string;
  socialLinks: Record<string, string>;
  aboutText: string;
}

export interface ContactMessage {
  id: string;
  name: string;
  phone: string;
  email: string;
  message: string;
  createdAt: number;
}

export interface AppNotification {
  id: string;
  recipientUserId?: string; // empty means all admins
  isAdminAlert?: boolean;
  title: string;
  message: string;
  type: string;
  relatedBookingId?: string;
  read: boolean;
  createdAt: number;
}

export type GalleryStatus = 'UNAVAILABLE' | 'PROCESSING' | 'READY';

export interface BookingUpdate {
  timestamp: number;
  title: string;
  message: string;
}

export interface CustomerBookingData extends Booking {
  updates: BookingUpdate[];
  galleryStatus: GalleryStatus;
  galleryMediaUrls: string[];
  invoice?: Invoice | null;
}

export type ScreenId =
  | 'home'
  | 'portfolio'
  | 'services'
  | 'service_detail'
  | 'packages'
  | 'package_detail'
  | 'booking'
  | 'customer_area'
  | 'private_gallery'
  | 'reviews'
  | 'about'
  | 'contact'
  | 'admin';

export type AdminSection =
  | 'DASHBOARD'
  | 'BOOKINGS'
  | 'INVOICES'
  | 'PORTFOLIO'
  | 'SERVICES'
  | 'PACKAGES'
  | 'REVIEWS'
  | 'BLOCKED_DATES'
  | 'SETTINGS';
