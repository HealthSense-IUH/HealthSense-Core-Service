package fit.iuh.se.hsassistant.repository;

import fit.iuh.se.hsassistant.entity.KnowledgeDocument;
import fit.iuh.se.hsassistant.entity.KnowledgeDocumentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KnowledgeDocumentRepository extends JpaRepository<KnowledgeDocument, String> {

	Page<KnowledgeDocument> findByTitleContainingIgnoreCaseOrFileNameContainingIgnoreCase(
			String title, String fileName, Pageable pageable);

	long countByStatus(KnowledgeDocumentStatus status);
}
