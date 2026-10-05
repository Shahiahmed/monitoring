package com.example.monitoring.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StaticPagesController {

    @GetMapping({"/login", "/login/"})
    public String login() {
        return "forward:/login/index.html";
    }

    @GetMapping({"/profile", "/profile/"})
    public String profile() {
        return "forward:/profile/index.html";
    }

    @GetMapping({"/servers", "/servers/"})
    public String servers() {
        return "forward:/servers/index.html";
    }

    @GetMapping({"/servers/detail", "/servers/detail/"})
    public String serversDetail() {
        return "forward:/servers/detail/index.html";
    }

    @GetMapping({"/users", "/users/"})
    public String users() {
        return "forward:/users/index.html";
    }

    @GetMapping({"/users/register", "/users/register/"})
    public String usersRegister() {
        return "forward:/users/register/index.html";
    }

    @GetMapping({"/users/roles", "/users/roles/"})
    public String usersRoles() {
        return "forward:/users/roles/index.html";
    }

    @GetMapping({"/settings", "/settings/"})
    public String settings() {
        return "forward:/settings/activity/index.html";
    }

    @GetMapping({"/settings/activity", "/settings/activity/"})
    public String settingsActivity() {
        return "forward:/settings/activity/index.html";
    }

    @GetMapping({"/settings/ssl", "/settings/ssl/"})
    public String settingsSsl() {
        return "forward:/settings/ssl/index.html";
    }

    @GetMapping({"/services", "/services/"})
    public String services() {
        return "forward:/services/index.html";
    }

    @GetMapping({"/services/add", "/services/add/"})
    public String servicesAdd() {
        return "forward:/services/add/index.html";
    }

    @GetMapping({"/services/registry", "/services/registry/"})
    public String servicesRegistry() {
        return "forward:/services/registry/index.html";
    }

    @GetMapping({"/references", "/references/"})
    public String references() {
        return "forward:/references/environments/index.html";
    }

    @GetMapping({"/references/environments", "/references/environments/"})
    public String referencesEnvironments() {
        return "forward:/references/environments/index.html";
    }

    @GetMapping({"/references/application-types", "/references/application-types/"})
    public String referencesApplicationTypes() {
        return "forward:/references/application-types/index.html";
    }

    @GetMapping({"/references/failure-types", "/references/failure-types/"})
    public String referencesFailureTypes() {
        return "forward:/references/failure-types/index.html";
    }

    @GetMapping({"/references/interaction-types", "/references/interaction-types/"})
    public String referencesInteractionTypes() {
        return "forward:/references/interaction-types/index.html";
    }

    @GetMapping({"/references/information-systems", "/references/information-systems/"})
    public String referencesInformationSystems() {
        return "forward:/references/information-systems/index.html";
    }

    @GetMapping({"/references/locations", "/references/locations/"})
    public String referencesLocations() {
        return "forward:/references/locations/index.html";
    }

    @GetMapping({"/references/government-bodies", "/references/government-bodies/"})
    public String referencesGovernmentBodies() {
        return "forward:/references/government-bodies/index.html";
    }

    @GetMapping({"/incidents", "/incidents/"})
    public String incidents() {
        return "forward:/incidents/index.html";
    }

    @GetMapping({"/incidents/add", "/incidents/add/"})
    public String incidentsAdd() {
        return "forward:/incidents/add/index.html";
    }

    @GetMapping({"/incidents/add/works", "/incidents/add/works/"})
    public String incidentsAddWorks() {
        return "forward:/incidents/add/works/index.html";
    }

    @GetMapping({"/incidents/add/incident", "/incidents/add/incident/"})
    public String incidentsAddIncident() {
        return "forward:/incidents/add/incident/index.html";
    }

    @GetMapping({"/incidents/add/prtg", "/incidents/add/prtg/"})
    public String incidentsAddPrtg() {
        return "forward:/incidents/add/prtg/index.html";
    }

    @GetMapping({"/incidents/events", "/incidents/events/"})
    public String incidentsEvents() {
        return "forward:/incidents/events/index.html";
    }

    @GetMapping({"/incidents/statistics", "/incidents/statistics/"})
    public String incidentsStatistics() {
        return "forward:/incidents/statistics/index.html";
    }

    @GetMapping({"/incidents/availability", "/incidents/availability/"})
    public String incidentsAvailability() {
        return "forward:/incidents/availability/index.html";
    }

    @GetMapping({"/changelog", "/changelog/"})
    public String changelog() {
        return "forward:/changelog/index.html";
    }

    @GetMapping({"/forgot-password", "/forgot-password/"})
    public String forgotPassword() {
        return "forward:/forgot-password/index.html";
    }

    @GetMapping({"/statistics/integrations", "/statistics/integrations/"})
    public String statisticsIntegrations() {
        return "forward:/statistics/integrations/index.html";
    }

    @GetMapping({"/wall/integrations", "/wall/integrations/"})
    public String wallIntegrations() {
        return "forward:/wall/integrations/index.html";
    }

    @GetMapping({"/services/my-services", "/services/my-services/"})
    public String servicesMyServices() {
        return "forward:/services/my-services/index.html";
    }

    @GetMapping({"/services/my-services/detail", "/services/my-services/detail/"})
    public String servicesMyServiceDetail() {
        return "forward:/services/my-services/detail/index.html";
    }

    @GetMapping({"/services/my-services/add", "/services/my-services/add/"})
    public String servicesMyServicesAdd() {
        return "forward:/services/my-services/add/index.html";
    }

    @GetMapping({"/services/my-services/client", "/services/my-services/client/"})
    public String servicesMyServiceClient() {
        return "forward:/services/my-services/client/index.html";
    }

    @GetMapping({"/services/my-connections", "/services/my-connections/"})
    public String servicesMyConnections() {
        return "forward:/services/my-connections/index.html";
    }
}

