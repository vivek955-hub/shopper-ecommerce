// Single place for API settings and small helpers shared by the components.

export const API_BASE_URL = (import.meta.env.VITE_API_URL || "http://localhost:8080/api").replace(/\/$/, "");

/** URL of a product's image. `version` can be used to bypass the browser cache after an update. */
export const productImageUrl = (id, version) =>
  `${API_BASE_URL}/product/${id}/image${version ? `?v=${version}` : ""}`;

const placeholderSvg =
  "<svg xmlns='http://www.w3.org/2000/svg' width='300' height='200'>" +
  "<rect width='100%' height='100%' fill='#e9ecef'/>" +
  "<text x='50%' y='50%' dominant-baseline='middle' text-anchor='middle' " +
  "fill='#6c757d' font-family='sans-serif' font-size='16'>No image</text></svg>";

export const PLACEHOLDER_IMAGE = `data:image/svg+xml;charset=utf-8,${encodeURIComponent(placeholderSvg)}`;

/** Use as onError on <img>: swaps a broken image for the placeholder (once, to avoid loops). */
export const handleImageError = (event) => {
  event.currentTarget.onerror = null;
  event.currentTarget.src = PLACEHOLDER_IMAGE;
};

/** Formats a money value with two decimals (avoids 0.1 * 3 = 0.30000000000000004 on screen). */
export const money = (value) => Number(value).toFixed(2);

/** Turns an API error into a readable message (field errors from the backend are listed). */
export const getErrorMessage = (error) => {
  const data = error?.response?.data;
  if (data?.errors && Object.keys(data.errors).length > 0) {
    return Object.values(data.errors).join(". ");
  }
  return data?.message || error?.message || "Something went wrong";
};
