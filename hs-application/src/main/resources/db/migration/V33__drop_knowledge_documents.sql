-- Bỏ tính năng kho tri thức (RAG): trợ lý chỉ còn chat kèm tool đọc dữ liệu người dùng.
-- V32 giữ nguyên vì đã chạy trên các DB; migration này dọn bảng nó tạo ra.
DROP TABLE IF EXISTS knowledge_documents;
