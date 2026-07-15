import { useCurrentUser, useLogout } from "../hooks/useAuth";
import { useFeed } from "../hooks/useFeed";
import { HeaderNav } from "../components/feed/HeaderNav";
import { MobileHeader } from "../components/feed/MobileHeader";
import { MobileBottomNav } from "../components/feed/MobileBottomNav";
import { LeftSidebar } from "../components/feed/LeftSidebar";
import { RightSidebar } from "../components/feed/RightSidebar";
import { StoriesBar } from "../components/feed/StoriesBar";
import { PostComposer } from "../components/feed/PostComposer";
import { PostCard } from "../components/feed/PostCard";

export function FeedPage() {
  const { data: user } = useCurrentUser();
  const logout = useLogout();
  const { data, isLoading, isError, fetchNextPage, hasNextPage, isFetchingNextPage } = useFeed();

  const posts = data?.pages.flatMap((page) => page.items) ?? [];

  return (
    <div className="_layout _layout_main_wrapper">
      <div className="_main_layout">
        <HeaderNav user={user} onLogout={() => logout.mutate()} />
        <MobileHeader user={user} onLogout={() => logout.mutate()} />

        <div className="container _custom_container">
          <div className="_layout_inner_wrap">
            <div className="row">
              <LeftSidebar />

              <div className="col-xl-6 col-lg-6 col-md-12 col-sm-12">
                <div className="_layout_middle_wrap">
                  <div className="_layout_middle_inner">
                    <StoriesBar />

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
              </div>

              <RightSidebar />
            </div>
          </div>
        </div>
      </div>

      <MobileBottomNav />
    </div>
  );
}
