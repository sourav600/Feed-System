import { type ChangeEvent, type FormEvent, useRef, useState } from "react";
import { useCreatePost } from "../../hooks/useFeed";
import type { Visibility } from "../../api/types";

export function PostComposer() {
  const [content, setContent] = useState("");
  const [visibility, setVisibility] = useState<Visibility>("PUBLIC");
  const [image, setImage] = useState<File | null>(null);
  const [imagePreview, setImagePreview] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const createPost = useCreatePost();

  function handleImageChange(event: ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0] ?? null;
    setImage(file);
    setImagePreview(file ? URL.createObjectURL(file) : null);
  }

  function clearImage() {
    setImage(null);
    setImagePreview(null);
    if (fileInputRef.current) fileInputRef.current.value = "";
  }

  function handleSubmit(event: FormEvent) {
    event.preventDefault();
    if (!content.trim()) return;
    createPost.mutate(
      { content, visibility, image },
      {
        onSuccess: () => {
          setContent("");
          setVisibility("PUBLIC");
          clearImage();
        },
      },
    );
  }

  return (
    <div className="_feed_inner_text_area _b_radious6 _padd_b24 _padd_t24 _padd_r24 _padd_l24 _mar_b16">
      <form onSubmit={handleSubmit}>
        <div className="_feed_inner_text_area_box">
          <textarea
            className="form-control"
            placeholder="Write something..."
            value={content}
            onChange={(e) => setContent(e.target.value)}
            rows={3}
            style={{ width: "100%", border: "none", resize: "vertical" }}
          />
        </div>

        {imagePreview && (
          <div style={{ position: "relative", marginTop: 10, marginBottom: 10 }}>
            <img src={imagePreview} alt="Selected" style={{ maxWidth: "100%", borderRadius: 8 }} />
            <button
              type="button"
              onClick={clearImage}
              style={{ position: "absolute", top: 8, right: 8, background: "#112032", color: "#fff", border: "none", borderRadius: "50%", width: 28, height: 28 }}
            >
              ×
            </button>
          </div>
        )}

        <div className="_feed_inner_text_area_bottom" style={{ display: "flex", alignItems: "center", justifyContent: "space-between", marginTop: 12 }}>
          <div className="_feed_inner_text_area_item" style={{ display: "flex", gap: 12, alignItems: "center" }}>
            <div className="_feed_inner_text_area_bottom_photo _feed_common">
              <button type="button" className="_feed_inner_text_area_bottom_photo_link" onClick={() => fileInputRef.current?.click()}>
                <span className="_feed_inner_text_area_bottom_photo_iamge _mar_img">🖼️</span> Photo
              </button>
              <input ref={fileInputRef} type="file" accept="image/png,image/jpeg,image/gif,image/webp" onChange={handleImageChange} hidden />
            </div>

            <div title={visibility === "PRIVATE" ? "Only you can see this post" : "Everyone can see this post"}>
              <button
                type="button"
                className="_feed_inner_text_area_bottom_photo_link"
                onClick={() => setVisibility((v) => (v === "PUBLIC" ? "PRIVATE" : "PUBLIC"))}
              >
                <span className="_feed_inner_text_area_bottom_photo_iamge _mar_img">
                  {visibility === "PRIVATE" ? "🔒" : "🌐"}
                </span>
                {visibility === "PRIVATE" ? "Private" : "Public"}
              </button>
            </div>
          </div>

          <div className="_feed_inner_text_area_btn">
            <button type="submit" className="_feed_inner_text_area_btn_link" disabled={createPost.isPending || !content.trim()}>
              {createPost.isPending ? "Posting..." : "Post"}
            </button>
          </div>
        </div>
      </form>
    </div>
  );
}
