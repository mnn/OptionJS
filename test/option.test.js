const chai = require('chai');
const spies = require('chai-spies');
chai.use(spies);
const should = chai.should();
require('../target/scala-2.11/optionjs-fastopt');

describe('Option', function() {
  it('picks right class', function() {
    Option('holodeck').should.be.instanceOf(Some);
    Option({}).should.be.instanceOf(Some);
    Option(0).should.be.instanceOf(Some);
    Option().should.be.instanceOf(None);
    Option(undefined).should.be.instanceOf(None);
    Option(null).should.be.instanceOf(None);
    Option(NaN).should.be.instanceOf(None);
  });

  it('map', function() {
    Option('Spock').map(x => 'Mr. ' + x).get.should.be.equal('Mr. Spock');
    should.not.exist(Option(null).map(x => 'Mr. ' + x).orNull);
  });

  it('foreach', function() {
    const someHandler = chai.spy();
    Option('Bones').forEach(someHandler);
    someHandler.should.have.been.called.once.always.with.exactly('Bones');

    const noneHandler = chai.spy();
    Option().foreach(noneHandler);
    noneHandler.should.have.not.been.called();
  });

  it('getOrElse', function() {
    Option(5).getOrElse(1).should.be.equal(5);
    Option().getOrElse(1).should.be.equal(1);
  });

  it('orElse', function() {
    Option(5).orElse(Option(4)).get.should.be.equal(5);
    Option(null).orElse(Option(4)).get.should.be.equal(4);
  });

  it('orNull', function() {
    Option('').orNull.should.be.equal('');
    Option(1).orNull.should.be.equal(1);
    should.not.exist(Option().orNull);
  });

  it('isEmpty', function() {
    Option().isEmpty.should.be.true;
    Option(null).isEmpty.should.be.true;
    Option('').isEmpty.should.be.false;
    Option(1).isEmpty.should.be.false;
  });

  it('isDefined', function() {
    Option('').isDefined.should.be.true;
    Option(null).isDefined.should.be.false;
  });

  it('nonEmpty', function() {
    Option('').nonEmpty.should.be.true;
    Option(null).nonEmpty.should.be.false;
  });

  it('count', function() {
    Option('').count(x => true).should.be.equal(1);
    Option(null).count(x => true).should.be.equal(0);
  });

  it('size', function() {
    Option('').size.should.be.equal(1);
    Option(null).size.should.be.equal(0);
  });

  it('filter', function() {
    Option(2).filter(x => x > 0).get.should.be.equal(2);
    should.not.exist(Option(2).filter(x => x > 4).orNull);
    should.not.exist(Option(null).filter(x => true).orNull);
  });

  it('filterNot', function() {
    should.not.exist(Option(2).filterNot(x => x > 0).orNull);
    Option(2).filterNot(x => x > 4).orNull.should.be.equal(2);
    should.not.exist(Option(null).filterNot(x => true).orNull);
  });

  it('find', function() {
    Option(2).find(x => x > 0).get.should.be.equal(2);
    should.not.exist(Option(2).find(x => x > 4).orNull);
    should.not.exist(Option(null).find(x => true).orNull);
  });

  it('exists', function() {
    Option(2).exists(x => x > 0).should.be.true;
    Option(2).exists(x => x > 4).should.be.false;
    Option(null).exists(() => true).should.be.false;
  });

  it('forall', function() {
    Option(2).forall(x => x > 0).should.be.true;
    Option(2).forall(x => x > 4).should.be.false;
    Option(null).forall(() => true).should.be.false;
  });

  it('flatten', function() {
    const flat = Option(Option('a')).flatten;
    flat.should.be.instanceOf(Some);
    flat.get.should.be.equal('a');
    Option(Option(null)).flatten.should.be.instanceOf(None);
    (() => Option('').flatten).should.throw();
  });

  it('flatMap', function() {
    Option('a').flatMap(x => Option(x + 2)).get.should.be.equal('a2');
    Option('a').flatMap(x => Option()).should.be.instanceOf(None);
  });

  it('mapIf', function() {
    const add = x => x + 1;
    var moreOne = x => x > 1;
    var moreTen = x => x > 10;
    Option(5).mapIf(moreOne, add).get.should.be.equal(6);
    Option(5).mapIf(moreTen, add).get.should.be.equal(5);
    should.not.exist(Option(null).mapIf(() => true, () => 5).orNull);
    Option(5).mapIf(true, add).get.should.be.equal(6);
    Option(5).mapIf(false, add).get.should.be.equal(5);
  });

  it('match', function() {
    const test = (optionInput, isSome, expectedReturnValue) => {
      let params = null;
      const someHandler = chai.spy((...args) => {
        params = args;
        return args[0].toString();
      });
      const noneHandler = chai.spy((...args) => {
        params = args;
        return Option(args[0].noneValue).map(x => x.toString()).getOrElse('null');
      });
      const res = Option(optionInput).match(someHandler, noneHandler);
      res.should.be.equal(expectedReturnValue);
      const expectHandler = isSome ? someHandler : noneHandler;
      const notExpectedHandler = isSome ? noneHandler : someHandler;
      expectHandler.should.have.been.called.once;
      notExpectedHandler.should.have.not.been.called();
      should.exist(params);
      if (isSome) {
        params[0].should.be.equal(optionInput);
        params[1].should.be.instanceOf(Some);
        params[1].get.should.be.equal(optionInput);
      } else {
        params[0].should.be.instanceOf(None);
      }
    };
    test(2, true, '2');
    test('a', true, 'a');
    test(null, false, 'null');
  });

  it('contains', function() {
    Option('a').contains('a').should.be.true;
    Option('a').contains('b').should.be.false;
    Option(1).contains(1).should.be.true;
    Option(null).contains(null).should.be.false;
    Some(null).contains(null).should.be.true;
    None(null).contains(null).should.be.false;
  });

  it('toArray', function() {
    Option('a').toArray().should.be.eql(['a']);
    Option(null).toArray().should.be.eql([]);
  });

  it('toObject', function() {
    Option('a').toObject().should.be.eql({value: 'a'});
    Option('a').toObject('moo').should.be.eql({moo: 'a'});
    Option(null).toObject().should.be.eql({});
  });
});
