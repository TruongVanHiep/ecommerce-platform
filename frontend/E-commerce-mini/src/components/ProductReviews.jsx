import { useContext, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { AuthContext } from "../context/AuthContext";
import { getReviewsByProduct, createReview, updateReview, deleteReview } from "../services/reviewService";

function Stars({ value, onChange }) {
  return (
    <div className="flex items-center gap-1">
      {[1, 2, 3, 4, 5].map((n) => (
        <button
          key={n}
          type="button"
          disabled={!onChange}
          onClick={() => onChange && onChange(n)}
          className={`text-xl leading-none ${onChange ? "cursor-pointer" : "cursor-default"} ${
            n <= value ? "text-yellow-400" : "text-slate-200"
          }`}
        >
          ★
        </button>
      ))}
    </div>
  );
}

export default function ProductReviews({ productId }) {
  const { user } = useContext(AuthContext);
  const [reviews, setReviews] = useState([]);
  const [loading, setLoading] = useState(true);

  const [rating, setRating] = useState(5);
  const [comment, setComment] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [formError, setFormError] = useState("");
  const [editingId, setEditingId] = useState(null);

  const loadReviews = async () => {
    try {
      setLoading(true);
      const res = await getReviewsByProduct(productId, 0, 20);
      setReviews(res.result?.content || []);
    } catch (err) {
      console.error("Error loading reviews:", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (productId) loadReviews();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [productId]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!comment.trim()) {
      setFormError("Vui lòng nhập nội dung đánh giá.");
      return;
    }
    setSubmitting(true);
    setFormError("");
    try {
      if (editingId) {
        await updateReview(editingId, { productId: Number(productId), rating, comment });
      } else {
        await createReview({ productId: Number(productId), rating, comment });
      }
      setComment("");
      setRating(5);
      setEditingId(null);
      await loadReviews();
    } catch (err) {
      console.error("Error submitting review:", err);
      setFormError(err.response?.data?.message || "Không thể gửi đánh giá.");
    } finally {
      setSubmitting(false);
    }
  };

  const handleEdit = (review) => {
    setEditingId(review.id);
    setRating(review.rating);
    setComment(review.comment || "");
    setFormError("");
  };

  const handleDelete = async (id) => {
    if (!confirm("Xóa đánh giá này?")) return;
    try {
      await deleteReview(id);
      if (editingId === id) {
        setEditingId(null);
        setComment("");
        setRating(5);
      }
      await loadReviews();
    } catch (err) {
      console.error("Error deleting review:", err);
    }
  };

  const avgRating = reviews.length
    ? (reviews.reduce((sum, r) => sum + r.rating, 0) / reviews.length).toFixed(1)
    : null;

  return (
    <div className="bg-white rounded-3xl shadow-sm p-6 sm:p-8 mt-6">
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-xl font-bold text-slate-900">Đánh giá sản phẩm</h2>
        {avgRating && (
          <div className="flex items-center gap-2">
            <Stars value={Math.round(avgRating)} />
            <span className="text-sm font-bold text-slate-700">{avgRating}/5</span>
            <span className="text-xs text-slate-400">({reviews.length} đánh giá)</span>
          </div>
        )}
      </div>

      {user ? (
        <form onSubmit={handleSubmit} className="mb-8 bg-slate-50 rounded-2xl p-5">
          <p className="text-sm font-semibold text-slate-700 mb-2">
            {editingId ? "Chỉnh sửa đánh giá của bạn" : "Viết đánh giá của bạn"}
          </p>
          <Stars value={rating} onChange={setRating} />
          <textarea
            value={comment}
            onChange={(e) => setComment(e.target.value)}
            rows="3"
            placeholder="Chia sẻ cảm nhận của bạn về sản phẩm này..."
            className="w-full mt-3 px-3 py-2.5 border border-slate-200 rounded-lg text-sm outline-none focus:border-indigo-500 transition-all resize-none"
          />
          {formError && <p className="text-red-500 text-xs font-semibold mt-2">{formError}</p>}
          <div className="flex items-center gap-3 mt-3">
            <button
              type="submit"
              disabled={submitting}
              className="bg-indigo-600 hover:bg-indigo-700 text-white font-bold text-sm px-5 py-2 rounded-lg transition-colors disabled:opacity-60"
            >
              {submitting ? "Đang gửi..." : editingId ? "Cập nhật" : "Gửi đánh giá"}
            </button>
            {editingId && (
              <button
                type="button"
                onClick={() => { setEditingId(null); setComment(""); setRating(5); setFormError(""); }}
                className="text-slate-500 text-sm font-semibold hover:underline"
              >
                Hủy
              </button>
            )}
          </div>
        </form>
      ) : (
        <div className="mb-8 bg-slate-50 rounded-2xl p-5 text-sm text-slate-600">
          <Link to="/login" className="text-indigo-600 font-bold hover:underline">Đăng nhập</Link> để viết đánh giá.
        </div>
      )}

      {loading ? (
        <div className="flex justify-center py-8">
          <div className="w-8 h-8 border-4 border-indigo-100 border-t-indigo-600 rounded-full animate-spin" />
        </div>
      ) : reviews.length === 0 ? (
        <p className="text-sm text-slate-400 text-center py-8">Chưa có đánh giá nào cho sản phẩm này.</p>
      ) : (
        <div className="divide-y divide-slate-100">
          {reviews.map((review) => (
            <div key={review.id} className="py-4">
              <div className="flex items-center justify-between">
                <div>
                  <span className="font-bold text-slate-800 text-sm">{review.userFullName || "Người dùng"}</span>
                  <div className="mt-1"><Stars value={review.rating} /></div>
                </div>
                <div className="flex items-center gap-3">
                  <span className="text-xs text-slate-400">
                    {review.createdAt ? new Date(review.createdAt).toLocaleDateString("vi-VN") : ""}
                  </span>
                  {user?.username && user.username === review.username && (
                    <div className="flex items-center gap-2 text-xs font-semibold">
                      <button type="button" onClick={() => handleEdit(review)} className="text-indigo-600 hover:underline">Sửa</button>
                      <button type="button" onClick={() => handleDelete(review.id)} className="text-red-500 hover:underline">Xóa</button>
                    </div>
                  )}
                </div>
              </div>
              <p className="text-sm text-slate-600 mt-2">{review.comment}</p>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
