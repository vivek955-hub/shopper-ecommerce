import React, { useContext, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { Button } from "react-bootstrap";
import AppContext from "../Context/Context";
import API from "../axios";
import CheckoutPopup from "./CheckoutPopup";
import { productImageUrl, handleImageError, PLACEHOLDER_IMAGE, getErrorMessage, money } from "../config";

const Cart = () => {
  const { cart, removeFromCart, updateQuantity, clearCart, refreshCart } = useContext(AppContext);
  const [showModal, setShowModal] = useState(false);
  const [placing, setPlacing] = useState(false);
  const [checkoutError, setCheckoutError] = useState("");
  const [lastOrder, setLastOrder] = useState(null);

  // Refresh prices/stock from the server when the cart page opens.
  useEffect(() => {
    refreshCart();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const totalPrice = cart.reduce((sum, item) => sum + item.price * item.quantity, 0);
  const hasUnavailableItem = cart.some((item) => !item.productAvailable || item.stockQuantity < 1);

  const handleIncrease = (item) => {
    if (item.quantity >= item.stockQuantity) {
      alert("Cannot add more than available stock");
      return;
    }
    updateQuantity(item.id, item.quantity + 1);
  };

  const handleDecrease = (item) => {
    updateQuantity(item.id, item.quantity - 1);
  };

  const openCheckout = () => {
    setCheckoutError("");
    setShowModal(true);
  };

  // One request: the server checks stock, reserves it and saves the order in a single transaction.
  const handleCheckout = async () => {
    setPlacing(true);
    setCheckoutError("");
    try {
      const response = await API.post("/orders", {
        items: cart.map((item) => ({ productId: item.id, quantity: item.quantity })),
      });
      setLastOrder(response.data);
      clearCart();
      setShowModal(false);
    } catch (error) {
      setCheckoutError(getErrorMessage(error));
      await refreshCart(); // show the latest stock so the customer can adjust
    } finally {
      setPlacing(false);
    }
  };

  if (lastOrder && cart.length === 0) {
    return (
      <div className="cart-container">
        <div className="shopping-cart">
          <div className="title">Order placed</div>
          <div style={{ padding: "2rem" }}>
            <h4>Thank you! Your order #{lastOrder.id} has been placed.</h4>
            <ul style={{ marginTop: "1rem" }}>
              {lastOrder.items.map((line) => (
                <li key={line.productId}>
                  {line.productName} x {line.quantity} - ${money(line.lineTotal)}
                </li>
              ))}
            </ul>
            <h5 style={{ marginTop: "1rem" }}>Total: ${money(lastOrder.totalAmount)}</h5>
            <Link to="/" className="btn btn-primary mt-3">
              Continue shopping
            </Link>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="cart-container">
      <div className="shopping-cart">
        <div className="title">Shopping Bag</div>
        {cart.length === 0 ? (
          <div className="empty" style={{ textAlign: "left", padding: "2rem" }}>
            <h4>Your cart is empty</h4>
          </div>
        ) : (
          <>
            {cart.map((item) => (
              <li key={item.id} className="cart-item">
                <div className="item" style={{ display: "flex", alignContent: "center" }}>
                  <div>
                    <img
                      src={item.hasImage ? productImageUrl(item.id) : PLACEHOLDER_IMAGE}
                      onError={handleImageError}
                      alt={item.name}
                      className="cart-item-image"
                    />
                  </div>
                  <div className="description">
                    <span>{item.brand}</span>
                    <span>{item.name}</span>
                    {(!item.productAvailable || item.stockQuantity < 1) && (
                      <span className="text-danger">Out of stock</span>
                    )}
                  </div>

                  <div className="quantity">
                    <button className="plus-btn" type="button" onClick={() => handleIncrease(item)}>
                      <i className="bi bi-plus-square-fill"></i>
                    </button>
                    <input type="button" value={item.quantity} readOnly />
                    <button className="minus-btn" type="button" onClick={() => handleDecrease(item)}>
                      <i className="bi bi-dash-square-fill"></i>
                    </button>
                  </div>

                  <div className="total-price " style={{ textAlign: "center" }}>
                    ${money(item.price * item.quantity)}
                  </div>
                  <button className="remove-btn" onClick={() => removeFromCart(item.id)}>
                    <i className="bi bi-trash3-fill"></i>
                  </button>
                </div>
              </li>
            ))}
            <div className="total">Total: ${money(totalPrice)}</div>
            {hasUnavailableItem && (
              <p className="text-danger">Remove the out-of-stock items to continue.</p>
            )}
            <Button
              className="btn btn-primary"
              style={{ width: "100%" }}
              onClick={openCheckout}
              disabled={hasUnavailableItem}
            >
              Checkout
            </Button>
          </>
        )}
      </div>
      <CheckoutPopup
        show={showModal}
        handleClose={() => setShowModal(false)}
        cartItems={cart}
        totalPrice={totalPrice}
        handleCheckout={handleCheckout}
        placing={placing}
        error={checkoutError}
      />
    </div>
  );
};

export default Cart;
