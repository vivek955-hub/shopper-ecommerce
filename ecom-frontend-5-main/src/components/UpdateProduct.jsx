import { useState, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import API from "../axios";
import { productImageUrl, PLACEHOLDER_IMAGE, handleImageError, getErrorMessage } from "../config";

const UpdateProduct = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [loaded, setLoaded] = useState(false);
  const [hasImage, setHasImage] = useState(false);
  const [newImage, setNewImage] = useState(null);
  const [newImagePreview, setNewImagePreview] = useState("");
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);
  const [updateProduct, setUpdateProduct] = useState({
    name: "",
    description: "",
    brand: "",
    price: "",
    category: "",
    releaseDate: "",
    productAvailable: false,
    stockQuantity: "",
  });

  useEffect(() => {
    let cancelled = false;
    API.get(`/product/${id}`)
      .then((response) => {
        if (cancelled) return;
        const p = response.data;
        setUpdateProduct({
          name: p.name ?? "",
          description: p.description ?? "",
          brand: p.brand ?? "",
          price: p.price ?? "",
          category: p.category ?? "",
          releaseDate: p.releaseDate ?? "",
          productAvailable: Boolean(p.productAvailable),
          stockQuantity: p.stockQuantity ?? "",
        });
        setHasImage(Boolean(p.imageName));
        setLoaded(true);
      })
      .catch((err) => {
        if (!cancelled) setError(getErrorMessage(err));
      });
    return () => {
      cancelled = true;
    };
  }, [id]);

  // Local preview for a newly selected image (released when it changes or the page closes).
  useEffect(() => {
    if (!newImage) {
      setNewImagePreview("");
      return undefined;
    }
    const url = URL.createObjectURL(newImage);
    setNewImagePreview(url);
    return () => URL.revokeObjectURL(url);
  }, [newImage]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setUpdateProduct({ ...updateProduct, [name]: value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSaving(true);

    const payload = {
      ...updateProduct,
      price: updateProduct.price === "" ? null : Number(updateProduct.price),
      stockQuantity: updateProduct.stockQuantity === "" ? null : Number(updateProduct.stockQuantity),
      releaseDate: updateProduct.releaseDate || null,
    };

    const formData = new FormData();
    // The image is optional: if none is chosen the server keeps the current one.
    if (newImage) {
      formData.append("imageFile", newImage);
    }
    formData.append("product", new Blob([JSON.stringify(payload)], { type: "application/json" }));

    try {
      await API.put(`/product/${id}`, formData);
      alert("Product updated successfully!");
      navigate(`/product/${id}`);
    } catch (err) {
      console.error("Error updating product:", err);
      setError(getErrorMessage(err));
    } finally {
      setSaving(false);
    }
  };

  let imageSrc = PLACEHOLDER_IMAGE;
  if (newImagePreview) {
    imageSrc = newImagePreview;
  } else if (hasImage) {
    imageSrc = productImageUrl(id, Date.now());
  }

  return (
    <div className="update-product-container">
      <div className="center-container" style={{ marginTop: "7rem" }}>
        <h1>Update Product</h1>
        {error && (
          <div className="alert alert-danger" role="alert">
            {error}
          </div>
        )}
        {loaded && (
          <form className="row g-3 pt-1" onSubmit={handleSubmit}>
            <div className="col-md-6">
              <label className="form-label">
                <h6>Name</h6>
              </label>
              <input
                type="text"
                className="form-control"
                value={updateProduct.name}
                onChange={handleChange}
                name="name"
              />
            </div>
            <div className="col-md-6">
              <label className="form-label">
                <h6>Brand</h6>
              </label>
              <input
                type="text"
                name="brand"
                className="form-control"
                value={updateProduct.brand}
                onChange={handleChange}
                id="brand"
              />
            </div>
            <div className="col-12">
              <label className="form-label">
                <h6>Description</h6>
              </label>
              <input
                type="text"
                className="form-control"
                name="description"
                onChange={handleChange}
                value={updateProduct.description}
                id="description"
              />
            </div>
            <div className="col-5">
              <label className="form-label">
                <h6>Price</h6>
              </label>
              <input
                type="number"
                step="0.01"
                min="0"
                className="form-control"
                onChange={handleChange}
                value={updateProduct.price}
                name="price"
                id="price"
              />
            </div>
            <div className="col-md-6">
              <label className="form-label">
                <h6>Category</h6>
              </label>
              <select
                className="form-select"
                value={updateProduct.category}
                onChange={handleChange}
                name="category"
                id="category"
              >
                <option value="">Select category</option>
                <option value="Laptop">Laptop</option>
                <option value="Headphone">Headphone</option>
                <option value="Mobile">Mobile</option>
                <option value="Electronics">Electronics</option>
                <option value="Toys">Toys</option>
                <option value="Fashion">Fashion</option>
              </select>
            </div>

            <div className="col-md-4">
              <label className="form-label">
                <h6>Stock Quantity</h6>
              </label>
              <input
                type="number"
                min="0"
                className="form-control"
                onChange={handleChange}
                value={updateProduct.stockQuantity}
                name="stockQuantity"
                id="stockQuantity"
              />
            </div>
            <div className="col-md-8">
              <label className="form-label">
                <h6>Image</h6>
              </label>
              <img
                src={imageSrc}
                onError={handleImageError}
                alt={updateProduct.name}
                style={{
                  width: "100%",
                  height: "180px",
                  objectFit: "cover",
                  padding: "5px",
                  margin: "0",
                }}
              />
              <input
                className="form-control"
                type="file"
                accept="image/*"
                onChange={(e) => setNewImage(e.target.files[0] || null)}
                name="imageUrl"
                id="imageUrl"
              />
              <div className="form-text">Leave empty to keep the current image.</div>
            </div>
            <div className="col-12">
              <div className="form-check">
                <input
                  className="form-check-input"
                  type="checkbox"
                  name="productAvailable"
                  id="gridCheck"
                  checked={updateProduct.productAvailable}
                  onChange={(e) => setUpdateProduct({ ...updateProduct, productAvailable: e.target.checked })}
                />
                <label className="form-check-label">Product Available</label>
              </div>
            </div>

            <div className="col-12">
              <button type="submit" className="btn btn-primary" disabled={saving}>
                {saving ? "Saving..." : "Submit"}
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
};

export default UpdateProduct;
