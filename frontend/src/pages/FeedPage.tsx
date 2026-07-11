import { useCurrentUser, useLogout } from "../hooks/useAuth";
import { useFeed } from "../hooks/useFeed";
import { PostComposer } from "../components/feed/PostComposer";
import { PostCard } from "../components/feed/PostCard";

export function FeedPage() {
  const { data: user } = useCurrentUser();
  const logout = useLogout();
  const { data, isLoading, isError, fetchNextPage, hasNextPage, isFetchingNextPage } = useFeed();

  const posts = data?.pages.flatMap((page) => page.items) ?? [];

  return (
    <div className="_layout _layout_main_wrapper">
      <nav className="navbar navbar-expand-lg navbar-light _header_nav _padd_t10">
        <div className="container _custom_container" style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
          <div className="_logo_wrap">
            <img src="/assets/images/logo.svg" alt="News Feed" className="_nav_logo" />
          </div>
          <div className="_header_nav_profile" style={{ display: "flex", alignItems: "center", gap: 12 }}>
            <span className="_header_nav_para">
              {user ? `${user.firstName} ${user.lastName}` : ""}
            </span>
            <button type="button" className="_btn1" onClick={() => logout.mutate()} style={{ padding: "6px 16px" }}>
              Log out
            </button>
          </div>
        </div>
      </nav>

      <div className="container _custom_container" style={{ maxWidth: 640, margin: "24px auto" }}>
        <PostComposer />

        {isLoading && <p>Loading feed...</p>}
        {isError && <p>Could not load the feed. Please try again.</p>}
        {!isLoading && posts.length === 0 && <p>No posts yet. Be the first to share something.</p>}

        {posts.map((post) => (
          <PostCard key={post.id} post={post} />
        ))}

        {hasNextPage && (
          <div style={{ textAlign: "center", margin: "20px 0" }}>
            <button type="button" className="_btn1" onClick={() => fetchNextPage()} disabled={isFetchingNextPage}>
              {isFetchingNextPage ? "Loading..." : "Load more"}
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
