import API from "../axios";
import { useState, useEffect, useCallback, createContext } from "react";

const CART_KEY = "cart";
export const PAGE_SIZE = 12;

// Only what the cart needs. Prices and stock stored here are just for display:
// the server re-checks both when the order is placed.
const toCartItem = (product, quantity) => ({
  id: product.id,
  name: product.name,
  brand: product.brand,
  price: product.price,
  stockQuantity: product.stockQuantity,
  productAvailable: product.productAvailable,
  hasImage: Boolean(product.imageName) || Boolean(product.hasImage),
  quantity,
});

const loadCart = () => {
  try {
    const stored = JSON.parse(localStorage.getItem(CART_KEY));
    if (!Array.isArray(stored)) return [];
    return stored
      .filter((item) => item && item.id != null)
      .map((item) => toCartItem(item, Number(item.quantity) || 1));
  } catch {
    return [];
  }
};

const AppContext = createContext({
  data: [],
  isError: "",
  pagination: { page: 0, totalPages: 0, totalElements: 0 },
  cart: [],
  addToCart: () => {},
  removeFromCart: () => {},
  updateQuantity: () => {},
  clearCart: () => {},
  refreshData: () => {},
  refreshCart: () => {},
});

export const AppProvider = ({ children }) => {
  const [data, setData] = useState([]);
  const [isError, setIsError] = useState("");
  const [pagination, setPagination] = useState({ page: 0, totalPages: 0, totalElements: 0 });
  const [cart, setCart] = useState(loadCart);

  useEffect(() => {
    localStorage.setItem(CART_KEY, JSON.stringify(cart));
  }, [cart]);

  // Loads one page of products (optionally filtered by category) from the API.
  const refreshData = useCallback(async (page = 0, category = "") => {
    try {
      const response = await API.get("/products", {
        params: { page, size: PAGE_SIZE, category: category || undefined },
      });
      setData(response.data.content);
      setPagination({
        page: response.data.page,
        totalPages: response.data.totalPages,
        totalElements: response.data.totalElements,
      });
      setIsError("");
    } catch (error) {
      setIsError(error.message);
    }
  }, []);

  const addToCart = (product) => {
    setCart((current) => {
      const existing = current.find((item) => item.id === product.id);
      if (existing) {
        const maxQuantity = Math.max(existing.stockQuantity, 1);
        return current.map((item) =>
          item.id === product.id ? { ...item, quantity: Math.min(item.quantity + 1, maxQuantity) } : item
        );
      }
      return [...current, toCartItem(product, 1)];
    });
  };

  const removeFromCart = (productId) => {
    const id = Number(productId);
    setCart((current) => current.filter((item) => item.id !== id));
  };

  const updateQuantity = (productId, quantity) => {
    const id = Number(productId);
    setCart((current) =>
      current.map((item) => {
        if (item.id !== id) return item;
        const maxQuantity = Math.max(item.stockQuantity, 1);
        return { ...item, quantity: Math.min(Math.max(quantity, 1), maxQuantity) };
      })
    );
  };

  const clearCart = () => setCart([]);

  // Re-reads every cart item from the API so price/stock are current and deleted products drop out.
  const refreshCart = async () => {
    const refreshed = await Promise.all(
      cart.map(async (item) => {
        try {
          const { data: product } = await API.get(`/product/${item.id}`);
          const maxQuantity = Math.max(product.stockQuantity, 1);
          return toCartItem(product, Math.min(item.quantity, maxQuantity));
        } catch (error) {
          if (error.response && error.response.status === 404) return null;
          return item; // network problem: keep what we have
        }
      })
    );
    setCart(refreshed.filter(Boolean));
  };

  return (
    <AppContext.Provider
      value={{
        data,
        isError,
        pagination,
        cart,
        addToCart,
        removeFromCart,
        updateQuantity,
        clearCart,
        refreshData,
        refreshCart,
      }}
    >
      {children}
    </AppContext.Provider>
  );
};

export default AppContext;
