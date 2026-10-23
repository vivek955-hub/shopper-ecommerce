import React, { useContext, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import AppContext from "../Context/Context";
import unplugged from "../assets/unplugged.png";
import { productImageUrl, handleImageError, PLACEHOLDER_IMAGE } from "../config";

const Home = ({ selectedCategory }) => {
  const { data, isError, addToCart, refreshData, pagination } = useContext(AppContext);
  const [page, setPage] = useState(0);

  // Go back to the first page whenever the category changes.
  const [previousCategory, setPreviousCategory] = useState(selectedCategory);
  if (previousCategory !== selectedCategory) {
    setPreviousCategory(selectedCategory);
    setPage(0);
  }

  useEffect(() => {
    refreshData(page, selectedCategory);
  }, [page, selectedCategory, refreshData]);

  if (isError) {
    return (
      <h2 className="text-center" style={{ padding: "18rem" }}>
        <img src={unplugged} alt="Error" style={{ width: "100px", height: "100px" }} />
      </h2>
    );
  }

  return (
    <>
      <div
        className="grid"
        style={{
          marginTop: "64px",
          display: "grid",
          gridTemplateColumns: "repeat(auto-fit, minmax(250px, 1fr))",
          gap: "20px",
          padding: "20px",
        }}
      >
        {data.length === 0 ? (
          <h2
            className="text-center"
            style={{
              display: "flex",
              justifyContent: "center",
              alignItems: "center",
            }}
          >
            No Products Available
          </h2>
        ) : (
          data.map((product) => {
            const { id, brand, name, price, productAvailable, imageName } = product;
            return (
              <div
                className="card mb-3"
                style={{
                  width: "250px",
                  height: "360px",
                  boxShadow: "0 4px 8px rgba(0,0,0,0.1)",
                  borderRadius: "10px",
                  overflow: "hidden",
                  backgroundColor: productAvailable ? "#fff" : "#ccc",
                  display: "flex",
                  flexDirection: "column",
                  justifyContent: "flex-start",
                  alignItems: "stretch",
                }}
                key={id}
              >
                <Link to={`/product/${id}`} style={{ textDecoration: "none", color: "inherit" }}>
                  <img
                    src={imageName ? productImageUrl(id) : PLACEHOLDER_IMAGE}
                    onError={handleImageError}
                    alt={name}
                    loading="lazy"
                    style={{
                      width: "100%",
                      height: "150px",
                      objectFit: "cover",
                      padding: "5px",
                      margin: "0",
                      borderRadius: "10px 10px 10px 10px",
                    }}
                  />
                  <div
                    className="card-body"
                    style={{
                      flexGrow: 1,
                      display: "flex",
                      flexDirection: "column",
                      justifyContent: "space-between",
                      padding: "10px",
                    }}
                  >
                    <div>
                      <h5 className="card-title" style={{ margin: "0 0 10px 0", fontSize: "1.2rem" }}>
                        {name.toUpperCase()}
                      </h5>
                      <i className="card-brand" style={{ fontStyle: "italic", fontSize: "0.8rem" }}>
                        {"~ " + brand}
                      </i>
                    </div>
                    <hr className="hr-line" style={{ margin: "10px 0" }} />
                    <div className="home-cart-price">
                      <h5
                        className="card-text"
                        style={{ fontWeight: "600", fontSize: "1.1rem", marginBottom: "5px" }}
                      >
                        <i className="bi bi-currency-rupee"></i>
                        {price}
                      </h5>
                    </div>
                    <button
                      className="btn-hover color-9"
                      style={{ margin: "10px 25px 0px " }}
                      onClick={(e) => {
                        e.preventDefault();
                        addToCart(product);
                      }}
                      disabled={!productAvailable}
                    >
                      {productAvailable ? "Add to Cart" : "Out of Stock"}
                    </button>
                  </div>
                </Link>
              </div>
            );
          })
        )}
      </div>

      {pagination.totalPages > 1 && (
        <div className="d-flex justify-content-center align-items-center gap-3 pb-4">
          <button
            className="btn btn-outline-primary"
            disabled={page === 0}
            onClick={() => setPage(page - 1)}
          >
            Previous
          </button>
          <span>
            Page {page + 1} of {pagination.totalPages}
          </span>
          <button
            className="btn btn-outline-primary"
            disabled={page + 1 >= pagination.totalPages}
            onClick={() => setPage(page + 1)}
          >
            Next
          </button>
        </div>
      )}
    </>
  );
};

export default Home;
