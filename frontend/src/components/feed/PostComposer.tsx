import { type ChangeEvent, type FormEvent, useRef, useState } from "react";
import { useCurrentUser } from "../../hooks/useAuth";
import { useCreatePost } from "../../hooks/useFeed";
import type { Visibility } from "../../api/types";
import { ArticleIcon, EditPencilIcon, EventIcon, PhotoIcon, SendIcon, VideoIcon } from "./icons";

export function PostComposer() {
  const { data: user } = useCurrentUser();
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
          <div className="_feed_inner_text_area_box_image">
            <img src={user?.avatarUrl ?? "/assets/images/txt_img.png"} alt="" className="_txt_img" style={{ borderRadius: "50%", objectFit: "cover" }} />
          </div>
          <div className="form-floating _feed_inner_text_area_box_form">
            <textarea
              className="form-control _textarea"
              placeholder="Write something ..."
              id="floatingTextarea"
              value={content}
              onChange={(e) => setContent(e.target.value)}
            />
            <label className="_feed_textarea_label" htmlFor="floatingTextarea">
              Write something ...
              <EditPencilIcon />
            </label>
          </div>
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

        <div className="_feed_inner_text_area_bottom">
          <div className="_feed_inner_text_area_item">
            <div className="_feed_inner_text_area_bottom_photo _feed_common">
              <button type="button" className="_feed_inner_text_area_bottom_photo_link" onClick={() => fileInputRef.current?.click()}>
                <span className="_feed_inner_text_area_bottom_photo_iamge _mar_img">
                  <PhotoIcon />
                </span>
                Photo
              </button>
              <input ref={fileInputRef} type="file" accept="image/png,image/jpeg,image/gif,image/webp" onChange={handleImageChange} hidden />
            </div>
            <div className="_feed_inner_text_area_bottom_video _feed_common">
              <button type="button" className="_feed_inner_text_area_bottom_photo_link" disabled title="Coming soon">
                <span className="_feed_inner_text_area_bottom_photo_iamge _mar_img">
                  <VideoIcon />
                </span>
                Video
              </button>
            </div>
            <div className="_feed_inner_text_area_bottom_event _feed_common">
              <button type="button" className="_feed_inner_text_area_bottom_photo_link" disabled title="Coming soon">
                <span className="_feed_inner_text_area_bottom_photo_iamge _mar_img">
                  <EventIcon />
                </span>
                Event
              </button>
            </div>
            <div className="_feed_inner_text_area_bottom_article _feed_common">
              <button type="button" className="_feed_inner_text_area_bottom_photo_link" disabled title="Coming soon">
                <span className="_feed_inner_text_area_bottom_photo_iamge _mar_img">
                  <ArticleIcon />
                </span>
                Article
              </button>
            </div>
            <div className="_feed_common" title={visibility === "PRIVATE" ? "Only you can see this post" : "Everyone can see this post"}>
              <button
                type="button"
                className="_feed_inner_text_area_bottom_photo_link"
                onClick={() => setVisibility((v) => (v === "PUBLIC" ? "PRIVATE" : "PUBLIC"))}
              >
                <span className="_feed_inner_text_area_bottom_photo_iamge _mar_img">{visibility === "PRIVATE" ? "🔒" : "🌐"}</span>
                {visibility === "PRIVATE" ? "Private" : "Public"}
              </button>
            </div>
          </div>

          <div className="_feed_inner_text_area_btn">
            <button type="submit" className="_feed_inner_text_area_btn_link" disabled={createPost.isPending || !content.trim()}>
              <SendIcon className="_mar_img" />
              <span>{createPost.isPending ? "Posting..." : "Post"}</span>
            </button>
          </div>
        </div>
      </form>
    </div>
  );
}
