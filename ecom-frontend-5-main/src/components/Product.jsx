import { useNavigate, useParams } from "react-router-dom";
import { useContext, useEffect, useState } from "react";
import AppContext from "../Context/Context";
import API from "../axios";
import { productImageUrl, handleImageError, PLACEHOLDER_IMAGE, getErrorMessage } from "../config";

const Product = () => {
  const { id } = useParams();
  const { addToCart, removeFromCart, refreshData } = useContext(AppContext);
  const [product, setProduct] = useState(null);
  const [notFound, setNotFound] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    let cancelled = false;
    setProduct(null);
    setNotFound(false);

    API.get(`/product/${id}`)
      .then((response) => {
        if (!cancelled) setProduct(response.data);
      })
      .catch((error) => {
        if (cancelled) return;
        if (error.response && error.response.status === 404) setNotFound(true);
        console.error("Error fetching product:", error);
      });

    return () => {
      cancelled = true;
    };
  }, [id]);

  const deleteProduct = async () => {
    if (!window.confirm(`Delete "${product.name}"? This cannot be undone.`)) return;
    try {
      await API.delete(`/product/${id}`);
      removeFromCart(product.id);
      alert("Product deleted successfully");
      refreshData();
      navigate("/");
    } catch (error) {
      console.error("Error deleting product:", error);
      alert(getErrorMessage(error));
    }
  };

  const handleEditClick = () => {
    navigate(`/product/update/${id}`);
  };

  const handleAddToCart = () => {
    addToCart(product);
    alert("Product added to cart");
  };

  if (notFound) {
    return (
      <h2 className="text-center" style={{ padding: "10rem" }}>
        Product not found
      </h2>
    );
  }

  if (!product) {
    return (
      <h2 className="text-center" style={{ padding: "10rem" }}>
        Loading...
      </h2>
    );
  }

  return (
    <>
      <div className="containers" style={{ display: "flex" }}>
        <img
          className="left-column-img"
          src={product.imageName ? productImageUrl(product.id) : PLACEHOLDER_IMAGE}
          onError={handleImageError}
          alt={product.name}
          style={{ width: "50%", height: "auto" }}
        />

        <div className="right-column" style={{ width: "50%" }}>
          <div className="product-description">
            <div style={{ display: "flex", justifyContent: "space-between" }}>
              <span style={{ fontSize: "1.2rem", fontWeight: "lighter" }}>{product.category}</span>
              <div className="release-date" style={{ marginBottom: "2rem" }}>
                {product.releaseDate && (
                  <h6>
                    Listed :{" "}
                    <span>
                      <i> {new Date(product.releaseDate).toLocaleDateString()}</i>
                    </span>
                  </h6>
                )}
              </div>
            </div>

            <h1
              style={{
                fontSize: "2rem",
                marginBottom: "0.5rem",
                textTransform: "capitalize",
                letterSpacing: "1px",
              }}
            >
              {product.name}
            </h1>
            <i style={{ marginBottom: "3rem" }}>{product.brand}</i>
            <p style={{ fontWeight: "bold", fontSize: "1rem", margin: "10px 0px 0px" }}>PRODUCT DESCRIPTION :</p>
            <p style={{ marginBottom: "1rem" }}>{product.description}</p>
          </div>

          <div className="product-price">
            <span style={{ fontSize: "2rem", fontWeight: "bold" }}>{"$" + product.price}</span>
            <button
              className={`cart-btn ${!product.productAvailable ? "disabled-btn" : ""}`}
              onClick={handleAddToCart}
              disabled={!product.productAvailable}
              style={{
                padding: "1rem 2rem",
                fontSize: "1rem",
                backgroundColor: "#007bff",
                color: "white",
                border: "none",
                borderRadius: "5px",
                cursor: "pointer",
                marginBottom: "1rem",
              }}
            >
              {product.productAvailable ? "Add to cart" : "Out of Stock"}
            </button>
            <h6 style={{ marginBottom: "1rem" }}>
              Stock Available :{" "}
              <i style={{ color: "green", fontWeight: "bold" }}>{product.stockQuantity}</i>
            </h6>
          </div>
          <div className="update-button" style={{ display: "flex", gap: "1rem" }}>
            <button
              className="btn btn-primary"
              type="button"
              onClick={handleEditClick}
              style={{
                padding: "1rem 2rem",
                fontSize: "1rem",
                backgroundColor: "#007bff",
                color: "white",
                border: "none",
                borderRadius: "5px",
                cursor: "pointer",
              }}
            >
              Update
            </button>
            <button
              className="btn btn-primary"
              type="button"
              onClick={deleteProduct}
              style={{
                padding: "1rem 2rem",
                fontSize: "1rem",
                backgroundColor: "#dc3545",
                color: "white",
                border: "none",
                borderRadius: "5px",
                cursor: "pointer",
              }}
            >
              Delete
            </button>
          </div>
        </div>
      </div>
    </>
  );
};

export default Product;
