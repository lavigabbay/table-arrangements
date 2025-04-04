import { type IUser } from '@/shared/model/user.model';

export interface IVenueTable {
  id?: number;
  numberOfTables?: number;
  nearStageTables?: number;
  venueName?: string;
  eventOwners?: string;
  groomParents?: string;
  brideParents?: string;
  weddingDate?: Date;
  receptionTime?: Date;
  weddingTime?: Date;
  user?: IUser | null;
}

export class VenueTable implements IVenueTable {
  constructor(
    public id?: number,
    public numberOfTables?: number,
    public nearStageTables?: number,
    public venueName?: string,
    public eventOwners?: string,
    public groomParents?: string,
    public brideParents?: string,
    public weddingDate?: Date,
    public receptionTime?: Date,
    public weddingTime?: Date,
    public user?: IUser | null,
  ) {}
}
