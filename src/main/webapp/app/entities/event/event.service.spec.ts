import axios from 'axios';
import sinon from 'sinon';
import dayjs from 'dayjs';

import EventService from './event.service';
import { DATE_FORMAT, DATE_TIME_FORMAT } from '@/shared/composables/date-format';
import { Event } from '@/shared/model/event.model';

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
  describe('Event Service', () => {
    let service: EventService;
    let elemDefault;
    let currentDate: Date;

    beforeEach(() => {
      service = new EventService();
      currentDate = new Date();
      elemDefault = new Event(123, 'AAAAAAA', 'AAAAAAA', 'AAAAAAA', 'AAAAAAA', currentDate, currentDate, currentDate);
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

      it('should create a Event', async () => {
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

      it('should not create a Event', async () => {
        axiosStub.post.rejects(error);

        return service
          .create({})
          .then()
          .catch(err => {
            expect(err).toMatchObject(error);
          });
      });

      it('should update a Event', async () => {
        const returnedFromService = {
          eventName: 'BBBBBB',
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

      it('should not update a Event', async () => {
        axiosStub.put.rejects(error);

        return service
          .update({})
          .then()
          .catch(err => {
            expect(err).toMatchObject(error);
          });
      });

      it('should partial update a Event', async () => {
        const patchObject = {
          eventName: 'BBBBBB',
          eventOwners: 'BBBBBB',
          groomParents: 'BBBBBB',
          brideParents: 'BBBBBB',
          receptionTime: dayjs(currentDate).format(DATE_TIME_FORMAT),
          ...new Event(),
        };
        const returnedFromService = Object.assign(patchObject, elemDefault);

        const expected = { weddingDate: currentDate, receptionTime: currentDate, weddingTime: currentDate, ...returnedFromService };
        axiosStub.patch.resolves({ data: returnedFromService });

        return service.partialUpdate(patchObject).then(res => {
          expect(res).toMatchObject(expected);
        });
      });

      it('should not partial update a Event', async () => {
        axiosStub.patch.rejects(error);

        return service
          .partialUpdate({})
          .then()
          .catch(err => {
            expect(err).toMatchObject(error);
          });
      });

      it('should return a list of Event', async () => {
        const returnedFromService = {
          eventName: 'BBBBBB',
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

      it('should not return a list of Event', async () => {
        axiosStub.get.rejects(error);

        return service
          .retrieve()
          .then()
          .catch(err => {
            expect(err).toMatchObject(error);
          });
      });

      it('should delete a Event', async () => {
        axiosStub.delete.resolves({ ok: true });
        return service.delete(123).then(res => {
          expect(res.ok).toBeTruthy();
        });
      });

      it('should not delete a Event', async () => {
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
