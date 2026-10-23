import React, { useContext, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import API from "../axios";
import AppContext from "../Context/Context";

const categories = ["Laptop", "Headphone", "Mobile", "Electronics", "Toys", "Fashion"];

const Navbar = ({ onSelectCategory }) => {
  const { cart } = useContext(AppContext);
  const [theme, setTheme] = useState(() => localStorage.getItem("theme") || "light-theme");
  const [input, setInput] = useState("");
  const [searchResults, setSearchResults] = useState([]);
  const [noResults, setNoResults] = useState(false);
  const [showSearchResults, setShowSearchResults] = useState(false);

  // Debounced search: wait 300ms after typing stops, and ignore out-of-date responses.
  useEffect(() => {
    const keyword = input.trim();
    if (!keyword) {
      setShowSearchResults(false);
      setSearchResults([]);
      setNoResults(false);
      return undefined;
    }

    setShowSearchResults(true);
    let cancelled = false;
    const timer = setTimeout(async () => {
      try {
        const response = await API.get("/products", { params: { keyword, size: 8 } });
        if (!cancelled) {
          setSearchResults(response.data.content);
          setNoResults(response.data.content.length === 0);
        }
      } catch (error) {
        console.error("Error searching:", error);
      }
    }, 300);

    return () => {
      cancelled = true;
      clearTimeout(timer);
    };
  }, [input]);

  const toggleTheme = () => {
    const newTheme = theme === "dark-theme" ? "light-theme" : "dark-theme";
    setTheme(newTheme);
    localStorage.setItem("theme", newTheme);
  };

  useEffect(() => {
    document.body.className = theme;
  }, [theme]);

  const cartCount = cart.reduce((sum, item) => sum + item.quantity, 0);

  return (
    <>
      <header>
        <nav className="navbar navbar-expand-lg fixed-top">
          <div className="container-fluid">
            <Link className="navbar-brand" to="/" onClick={() => onSelectCategory("")}>
              ShoppeR
            </Link>
            <button
              className="navbar-toggler"
              type="button"
              data-bs-toggle="collapse"
              data-bs-target="#navbarSupportedContent"
              aria-controls="navbarSupportedContent"
              aria-expanded="false"
              aria-label="Toggle navigation"
            >
              <span className="navbar-toggler-icon"></span>
            </button>
            <div className="collapse navbar-collapse" id="navbarSupportedContent">
              <ul className="navbar-nav me-auto mb-2 mb-lg-0">
                <li className="nav-item">
                  <Link className="nav-link active" aria-current="page" to="/">
                    Home
                  </Link>
                </li>
                <li className="nav-item">
                  <Link className="nav-link" to="/add_product">
                    Add Product
                  </Link>
                </li>

                <li className="nav-item dropdown">
                  <a
                    className="nav-link dropdown-toggle"
                    href="#"
                    role="button"
                    data-bs-toggle="dropdown"
                    aria-expanded="false"
                    onClick={(e) => e.preventDefault()}
                  >
                    Categories
                  </a>

                  <ul className="dropdown-menu">
                    <li>
                      <button className="dropdown-item" onClick={() => onSelectCategory("")}>
                        All categories
                      </button>
                    </li>
                    {categories.map((category) => (
                      <li key={category}>
                        <button className="dropdown-item" onClick={() => onSelectCategory(category)}>
                          {category}
                        </button>
                      </li>
                    ))}
                  </ul>
                </li>
              </ul>
              <button className="theme-btn" onClick={toggleTheme}>
                {theme === "dark-theme" ? (
                  <i className="bi bi-moon-fill"></i>
                ) : (
                  <i className="bi bi-sun-fill"></i>
                )}
              </button>
              <div className="d-flex align-items-center cart">
                <Link to="/cart" className="nav-link text-dark">
                  <i className="bi bi-cart me-2" style={{ display: "flex", alignItems: "center" }}>
                    Cart{cartCount > 0 ? ` (${cartCount})` : ""}
                  </i>
                </Link>
                <input
                  className="form-control me-2"
                  type="search"
                  placeholder="Search"
                  aria-label="Search"
                  value={input}
                  onChange={(e) => setInput(e.target.value)}
                />
                {showSearchResults && (
                  <ul className="list-group">
                    {searchResults.length > 0
                      ? searchResults.map((result) => (
                          <li key={result.id} className="list-group-item">
                            <Link
                              to={`/product/${result.id}`}
                              className="search-result-link"
                              onClick={() => setInput("")}
                            >
                              <span>{result.name}</span>
                            </Link>
                          </li>
                        ))
                      : noResults && <p className="no-results-message">No product with such name</p>}
                  </ul>
                )}
              </div>
            </div>
          </div>
        </nav>
      </header>
    </>
  );
};

export default Navbar;
