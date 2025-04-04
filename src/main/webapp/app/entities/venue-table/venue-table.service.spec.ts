import axios from 'axios';
import sinon from 'sinon';
import dayjs from 'dayjs';

import VenueTableService from './venue-table.service';
import { DATE_FORMAT, DATE_TIME_FORMAT } from '@/shared/composables/date-format';
import { VenueTable } from '@/shared/model/venue-table.model';

const error = {
  response: {
    status: null,
    data: {
      type: null,
    },
  },
};

const axiosStub = {
  get: sinon.stub(axios, 'get'),
  post: sinon.stub(axios, 'post'),
  put: sinon.stub(axios, 'put'),
  patch: sinon.stub(axios, 'patch'),
  delete: sinon.stub(axios, 'delete'),
};

describe('Service Tests', () => {
  describe('VenueTable Service', () => {
    let service: VenueTableService;
    let elemDefault;
    let currentDate: Date;

    beforeEach(() => {
      service = new VenueTableService();
      currentDate = new Date();
      elemDefault = new VenueTable(123, 0, 0, 'AAAAAAA', 'AAAAAAA', 'AAAAAAA', 'AAAAAAA', currentDate, currentDate, currentDate);
    });

    describe('Service methods', () => {
      it('should find an element', async () => {
        const returnedFromService = {
          weddingDate: dayjs(currentDate).format(DATE_FORMAT),
          receptionTime: dayjs(currentDate).format(DATE_TIME_FORMAT),
          weddingTime: dayjs(currentDate).format(DATE_TIME_FORMAT),
          ...elemDefault,
        };
        axiosStub.get.resolves({ data: returnedFromService });

        return service.find(123).then(res => {
          expect(res).toMatchObject(elemDefault);
        });
      });

      it('should not find an element', async () => {
        axiosStub.get.rejects(error);
        return service
          .find(123)
          .then()
          .catch(err => {
            expect(err).toMatchObject(error);
          });
      });

      it('should create a VenueTable', async () => {
        const returnedFromService = {
          id: 123,
          weddingDate: dayjs(currentDate).format(DATE_FORMAT),
          receptionTime: dayjs(currentDate).format(DATE_TIME_FORMAT),
          weddingTime: dayjs(currentDate).format(DATE_TIME_FORMAT),
          ...elemDefault,
        };
        const expected = { weddingDate: currentDate, receptionTime: currentDate, weddingTime: currentDate, ...returnedFromService };

        axiosStub.post.resolves({ data: returnedFromService });
        return service.create({}).then(res => {
          expect(res).toMatchObject(expected);
        });
      });

      it('should not create a VenueTable', async () => {
        axiosStub.post.rejects(error);

        return service
          .create({})
          .then()
          .catch(err => {
            expect(err).toMatchObject(error);
          });
      });

      it('should update a VenueTable', async () => {
        const returnedFromService = {
          numberOfTables: 1,
          nearStageTables: 1,
          venueName: 'BBBBBB',
          eventOwners: 'BBBBBB',
          groomParents: 'BBBBBB',
          brideParents: 'BBBBBB',
          weddingDate: dayjs(currentDate).format(DATE_FORMAT),
          receptionTime: dayjs(currentDate).format(DATE_TIME_FORMAT),
          weddingTime: dayjs(currentDate).format(DATE_TIME_FORMAT),
          ...elemDefault,
        };

        const expected = { weddingDate: currentDate, receptionTime: currentDate, weddingTime: currentDate, ...returnedFromService };
        axiosStub.put.resolves({ data: returnedFromService });

        return service.update(expected).then(res => {
          expect(res).toMatchObject(expected);
        });
      });

      it('should not update a VenueTable', async () => {
        axiosStub.put.rejects(error);

        return service
          .update({})
          .then()
          .catch(err => {
            expect(err).toMatchObject(error);
          });
      });

      it('should partial update a VenueTable', async () => {
        const patchObject = { numberOfTables: 1, nearStageTables: 1, eventOwners: 'BBBBBB', brideParents: 'BBBBBB', ...new VenueTable() };
        const returnedFromService = Object.assign(patchObject, elemDefault);

        const expected = { weddingDate: currentDate, receptionTime: currentDate, weddingTime: currentDate, ...returnedFromService };
        axiosStub.patch.resolves({ data: returnedFromService });

        return service.partialUpdate(patchObject).then(res => {
          expect(res).toMatchObject(expected);
        });
      });

      it('should not partial update a VenueTable', async () => {
        axiosStub.patch.rejects(error);

        return service
          .partialUpdate({})
          .then()
          .catch(err => {
            expect(err).toMatchObject(error);
          });
      });

      it('should return a list of VenueTable', async () => {
        const returnedFromService = {
          numberOfTables: 1,
          nearStageTables: 1,
          venueName: 'BBBBBB',
          eventOwners: 'BBBBBB',
          groomParents: 'BBBBBB',
          brideParents: 'BBBBBB',
          weddingDate: dayjs(currentDate).format(DATE_FORMAT),
          receptionTime: dayjs(currentDate).format(DATE_TIME_FORMAT),
          weddingTime: dayjs(currentDate).format(DATE_TIME_FORMAT),
          ...elemDefault,
        };
        const expected = { weddingDate: currentDate, receptionTime: currentDate, weddingTime: currentDate, ...returnedFromService };
        axiosStub.get.resolves([returnedFromService]);
        return service.retrieve({ sort: {}, page: 0, size: 10 }).then(res => {
          expect(res).toContainEqual(expected);
        });
      });

      it('should not return a list of VenueTable', async () => {
        axiosStub.get.rejects(error);

        return service
          .retrieve()
          .then()
          .catch(err => {
            expect(err).toMatchObject(error);
          });
      });

      it('should delete a VenueTable', async () => {
        axiosStub.delete.resolves({ ok: true });
        return service.delete(123).then(res => {
          expect(res.ok).toBeTruthy();
        });
      });

      it('should not delete a VenueTable', async () => {
        axiosStub.delete.rejects(error);

        return service
          .delete(123)
          .then()
          .catch(err => {
            expect(err).toMatchObject(error);
          });
      });
    });
  });
});
