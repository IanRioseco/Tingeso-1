// Define un conjunto de datos vacíos para la creación de paquetes turísticos.
export const EMPTY_PACKAGE_FORM = {
  name: '',
  destination: '',
  description: '',
  startDate: '',
  endDate: '',
  price: '',
  totalSlots: '',
  durationDays: '',
  travelType: '',
  season: '',
  restrictions: '',
  servicesIncluded: '',
  conditions: '',
};

// Convierte los datos de un paquete en un objeto de formulario.
export const packageToFormModel = (pkg) => ({
  name: pkg?.name || '',
  destination: pkg?.destination || '',
  description: pkg?.description || '',
  startDate: pkg?.startDate || '',
  endDate: pkg?.endDate || '',
  price: pkg?.price || '',
  totalSlots: pkg?.totalSlots || pkg?.availableSlots || '',
  durationDays: pkg?.durationDays || '',
  travelType: pkg?.travelType || '',
  season: pkg?.season || '',
  restrictions: pkg?.restrictions || '',
  servicesIncluded: pkg?.servicesIncluded || '',
  conditions: pkg?.conditions || '',
});

// Convierte los datos de un objeto de formulario en un objeto de solicitud.
export const formToPackagePayload = (form) => ({
  ...form,
  price: form.price ? form.price.toString().replace(/\./g, '') : '',
});
