package fit.iuh.se.hsassistant.service;

import fit.iuh.se.hsuser.entity.enums.UserRole;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.stereotype.Component;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Đọc prompts/pages.st (mỗi dòng: vai trò | đường dẫn | tên trang | mô tả) và trả về danh sách trang
 * mà một vai trò được mở, để trợ lý dẫn link đúng trang khi người dùng hỏi "ở đâu", "trang nào".
 */
@Slf4j
@Component
public class AssistantPageCatalog implements ResourceLoaderAware {

	public record Page(Set<String> roles, String path, String title, String description) {
		boolean allows(UserRole role) {
			return roles.contains("ALL") || (role != null && roles.contains(role.name()));
		}
	}

	private static final String LOCATION = "classpath:prompts/pages.st";

	private ResourceLoader resourceLoader;
	private List<Page> pages = List.of();

	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	@PostConstruct
	void load() throws IOException {
		Resource resource = ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResource(LOCATION);
		List<Page> parsed = new ArrayList<>();
		for (String raw : resource.getContentAsString(StandardCharsets.UTF_8).split("\\R")) {
			String line = raw.strip();
			if (line.isEmpty() || line.startsWith("#")) continue;
			String[] parts = line.split("\\|");
			if (parts.length < 4) {
				log.warn("pages.st: bỏ qua dòng không đủ 4 cột: {}", line);
				continue;
			}
			Set<String> roles = java.util.Arrays.stream(parts[0].split(","))
					.map(String::strip).filter(s -> !s.isEmpty()).collect(Collectors.toSet());
			parsed.add(new Page(roles, parts[1].strip(), parts[2].strip(), parts[3].strip()));
		}
		this.pages = List.copyOf(parsed);
		log.info("Assistant page catalog: {} trang", pages.size());
	}

	public List<Page> pagesFor(UserRole role) {
		return pages.stream().filter(p -> p.allows(role)).toList();
	}

	/** Dạng chữ để nhúng vào system prompt: "- Tên trang: /đường/dẫn — mô tả". */
	public String renderFor(UserRole role) {
		List<Page> allowed = pagesFor(role);
		if (allowed.isEmpty()) return "(không có trang nào)";
		return allowed.stream()
				.map(p -> "- " + p.title() + ": " + p.path() + " — " + p.description())
				.collect(Collectors.joining("\n"));
	}
}
