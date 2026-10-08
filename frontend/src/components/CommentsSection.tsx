import { useEffect, useState } from "react";
import type { FormEvent } from "react";
import { Avatar } from "./Avatar";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../context/ToastContext";
import { getErrorMessage } from "../services/api";
import * as commentService from "../services/commentService";
import type { Comment } from "../types/comment";

export function CommentsSection({
  taskId,
  canModerate,
}: {
  taskId: number;
  /** True if the current user can delete ANY comment here (project owner/admin), not just their own. */
  canModerate: boolean;
}) {
  const { user } = useAuth();
  const showToast = useToast();
  const [comments, setComments] = useState<Comment[] | null>(null);
  const [content, setContent] = useState("");
  const [isPosting, setIsPosting] = useState(false);

  useEffect(() => {
    commentService
      .listComments(taskId)
      .then(setComments)
      .catch((err) => showToast(getErrorMessage(err), "error"));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [taskId]);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    if (!content.trim()) return;
    setIsPosting(true);
    try {
      const comment = await commentService.createComment(taskId, content);
      setComments((prev) => [...(prev ?? []), comment]);
      setContent("");
    } catch (err) {
      showToast(getErrorMessage(err), "error");
    } finally {
      setIsPosting(false);
    }
  }

  async function handleDelete(commentId: number) {
    if (!confirm("Delete this comment?")) return;
    try {
      await commentService.deleteComment(commentId);
      setComments((prev) => prev!.filter((c) => c.id !== commentId));
    } catch (err) {
      showToast(getErrorMessage(err), "error");
    }
  }

  return (
    <div className="card" style={{ maxWidth: 520, marginTop: 16 }}>
      <h2 style={{ marginBottom: 12 }}>Comments</h2>

      {comments === null ? (
        "Loading..."
      ) : comments.length === 0 ? (
        <p style={{ color: "var(--text-muted)", marginBottom: 16 }}>No comments yet.</p>
      ) : (
        <div style={{ marginBottom: 16 }}>
          {comments.map((comment) => (
            <div key={comment.id} className="member-row" style={{ alignItems: "flex-start" }}>
              <div>
                <div style={{ display: "flex", alignItems: "center", gap: 8, marginBottom: 2 }}>
                  <Avatar user={comment.author} style={{ marginLeft: 0 }} />
                  <strong style={{ fontSize: 13 }}>
                    {comment.author.firstName} {comment.author.lastName}
                  </strong>
                  <span style={{ fontSize: 12, color: "var(--text-muted)" }}>
                    {new Date(comment.createdAt).toLocaleString()}
                  </span>
                </div>
                <p style={{ marginLeft: 36 }}>{comment.content}</p>
              </div>
              {(comment.author.id === user?.id || canModerate) && (
                <button className="btn btn-danger" onClick={() => handleDelete(comment.id)}>
                  Delete
                </button>
              )}
            </div>
          ))}
        </div>
      )}

      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <textarea
            rows={3}
            placeholder="Write a comment..."
            value={content}
            onChange={(e) => setContent(e.target.value)}
          />
        </div>
        <button type="submit" className="btn btn-primary" disabled={isPosting || !content.trim()}>
          {isPosting ? "Posting..." : "Post Comment"}
        </button>
      </form>
    </div>
  );
}
