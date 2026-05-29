package com.credithandler.dossier.service;

import com.credithandler.dossier.model.EmailTemplateModel;
import com.credithandler.dossier.model.RenderedEmail;

public interface EmailTemplateService {

    RenderedEmail render(EmailTemplateModel model);
}
