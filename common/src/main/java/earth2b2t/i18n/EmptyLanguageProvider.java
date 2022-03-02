package earth2b2t.i18n;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class EmptyLanguageProvider implements LanguageProvider {

    @Override
    public void update(UUID player, String preferred) {
        // do nothing
    }

    @Override
    public List<String> get(UUID player) {
        return Collections.emptyList();
    }
}
