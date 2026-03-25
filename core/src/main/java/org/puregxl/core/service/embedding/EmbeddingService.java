package org.puregxl.core.service.embedding;

import java.io.IOException;
import java.util.List;

public interface EmbeddingService {

    List<List<Float>> embed(List<String> texts) throws IOException;
}
