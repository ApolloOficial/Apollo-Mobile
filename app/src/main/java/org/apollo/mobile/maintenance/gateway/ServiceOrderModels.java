package org.apollo.mobile.maintenance.gateway;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ServiceOrderModels {
    private ServiceOrderModels() {
    }

    public enum Scope {
        PANEL,
        STRING,
        INVERTER
    }

    public static final class Page<T> {
        private List<T> content;
        private int number;
        private int totalPages;
        private long totalElements;

        public List<T> getContent() {
            return content == null ? Collections.emptyList() : content;
        }

        public int getNumber() {
            return number;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public long getTotalElements() {
            return totalElements;
        }
    }

    public static final class Order {
        private long id;
        private Long warningId;
        private String warningType;
        private String priority;
        private String maintenanceTypeName;
        private String technicianName;
        private String technicalReport;
        private String maintenanceStatus;
        private boolean overdue;
        private String openingDt;
        private String dueDate;
        private String startedAt;
        private String concludedAt;

        public long getId() { return id; }
        public Long getWarningId() { return warningId; }
        public String getWarningType() { return warningType; }
        public String getPriority() { return priority; }
        public String getMaintenanceTypeName() { return maintenanceTypeName; }
        public String getTechnicianName() { return technicianName; }
        public String getTechnicalReport() { return technicalReport; }
        public String getMaintenanceStatus() { return maintenanceStatus; }
        public boolean isOverdue() { return overdue; }
        public String getOpeningDt() { return openingDt; }
        public String getDueDate() { return dueDate; }
        public String getStartedAt() { return startedAt; }
        public String getConcludedAt() { return concludedAt; }
    }

    public static final class Warning {
        private long id;
        private String stringId;
        private String stringCode;
        private String inverterCode;
        private Long panelId;
        private String panelSerial;
        private String type;
        private String severity;
        private String message;

        public long getId() { return id; }
        public String getStringId() { return stringId; }
        public String getStringCode() { return stringCode; }
        public String getInverterCode() { return inverterCode; }
        public Long getPanelId() { return panelId; }
        public String getPanelSerial() { return panelSerial; }
        public String getType() { return type; }
        public String getSeverity() { return severity; }
        public String getMessage() { return message; }
    }

    public static final class SolarString {
        private String id;
        private String code;
        private String inverterCode;
        private long installedPanels;

        public String getId() { return id; }
        public String getCode() { return code; }
        public String getInverterCode() { return inverterCode; }
        public long getInstalledPanels() { return installedPanels; }
    }

    public static final class Panel {
        private long id;
        private String stringId;
        private String stringCode;
        private String inverterCode;
        private String serialNumber;
        private String barcode;
        private String operatingStats;

        public long getId() { return id; }
        public String getStringId() { return stringId; }
        public String getStringCode() { return stringCode; }
        public String getInverterCode() { return inverterCode; }
        public String getSerialNumber() { return serialNumber; }
        public String getBarcode() { return barcode; }
        public String getOperatingStats() { return operatingStats; }
    }

    public static final class CompleteBody {
        private final String technicalReport;
        private final double laborCost;

        public CompleteBody(String technicalReport, double laborCost) {
            this.technicalReport = technicalReport;
            this.laborCost = laborCost;
        }
    }

    public static final class MaintenanceType {
        private long id;
        private String name;
        private String description;
        private Integer defaultIntervalDays;

        public long getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public Integer getDefaultIntervalDays() { return defaultIntervalDays; }
    }

    public static final class StringGroup {
        private final SolarString solarString;
        private final List<Panel> panels;

        public StringGroup(SolarString solarString, List<Panel> panels) {
            this.solarString = solarString;
            this.panels = panels == null ? Collections.emptyList() : panels;
        }

        public SolarString getSolarString() { return solarString; }
        public List<Panel> getPanels() { return panels; }
    }

    public static final class Details {
        private final Order order;
        private final Warning warning;
        private final Scope scope;
        private final List<StringGroup> groups;

        public Details(Order order, Warning warning, Scope scope, List<StringGroup> groups) {
            this.order = order;
            this.warning = warning;
            this.scope = scope;
            this.groups = groups == null ? Collections.emptyList() : groups;
        }

        public Order getOrder() { return order; }
        public Warning getWarning() { return warning; }
        public Scope getScope() { return scope; }
        public List<StringGroup> getGroups() { return groups; }

        public List<Panel> getPanels() {
            List<Panel> result = new ArrayList<>();
            for (StringGroup group : groups) result.addAll(group.getPanels());
            return result;
        }

        public List<Panel> getTargetPanels() {
            if (scope != Scope.PANEL) return getPanels();
            List<Panel> result = new ArrayList<>();
            Long target = warning == null ? null : warning.getPanelId();
            for (Panel panel : getPanels()) {
                if (target != null && panel.getId() == target) result.add(panel);
            }
            return result;
        }

        public int positionOf(Panel panel) {
            for (StringGroup group : groups) {
                int index = group.getPanels().indexOf(panel);
                if (index >= 0) return index + 1;
            }
            return 0;
        }

        public Panel findPanel(long id) {
            for (Panel panel : getPanels()) {
                if (panel.getId() == id) return panel;
            }
            return null;
        }
    }
}
