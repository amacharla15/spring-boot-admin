export type Registration = {
  name: string;
  managementUrl?: string;
  healthUrl: string;
  serviceUrl?: string;
  source: string;
  metadata?: { [key: string]: string }[];
};

export type StatusInfo = {
  status:
    | 'UNKNOWN'
    | 'OUT_OF_SERVICE'
    | 'UP'
    | 'DOWN'
    | 'OFFLINE'
    | 'RESTRICTED'
    | string;
  details: { [key: string]: string };
};

export const DOWN_STATES = ['OUT_OF_SERVICE', 'DOWN', 'OFFLINE', 'RESTRICTED'];
export const UP_STATES = ['UP'];
export const UNKNOWN_STATES = ['UNKNOWN'];
