package arrow.mtl.test

import arrow.Kind
import arrow.core.Const
import arrow.core.ConstPartialOf
import arrow.core.ForListK
import arrow.core.ForOption
import arrow.core.Id
import arrow.core.ListK
import arrow.core.Option
import arrow.core.extensions.const.divisible.divisible
import arrow.core.extensions.const.eqK.eqK
import arrow.core.extensions.eq
import arrow.core.extensions.id.eqK.eqK
import arrow.core.extensions.listk.eq.eq
import arrow.core.extensions.listk.eqK.eqK
import arrow.core.extensions.listk.monoid.monoid
import arrow.core.extensions.listk.monoidK.monoidK
import arrow.core.extensions.monoid
import arrow.core.extensions.option.alternative.alternative
import arrow.core.extensions.option.applicative.applicative
import arrow.core.extensions.option.eqK.eqK
import arrow.core.extensions.option.functor.functor
import arrow.core.extensions.option.monad.monad
import arrow.core.extensions.option.monadFilter.monadFilter
import arrow.core.k
import arrow.core.test.UnitSpec
import arrow.core.test.generators.GenK
import arrow.core.test.generators.genK
import arrow.core.test.generators.tuple2
import arrow.core.test.laws.AlternativeLaws
import arrow.core.test.laws.DivisibleLaws
import arrow.core.test.laws.MonadFilterLaws
import arrow.core.test.laws.MonoidKLaws
import arrow.fx.ForIO
import arrow.fx.IO
import arrow.fx.extensions.io.applicative.applicative
import arrow.fx.extensions.io.concurrent.concurrent
import arrow.fx.extensions.io.functor.functor
import arrow.fx.extensions.io.monad.monad
import arrow.fx.mtl.concurrent
import arrow.fx.mtl.timer
import arrow.fx.test.laws.ConcurrentLaws
import arrow.mtl.ForWriterT
import arrow.mtl.WriterT
import arrow.mtl.WriterTPartialOf
import arrow.mtl.eq.EqTrans
import arrow.mtl.extensions.WriterTEqK
import arrow.mtl.extensions.core.monadBaseControl
import arrow.mtl.extensions.monadBaseControl
import arrow.mtl.extensions.monadTransControl
import arrow.mtl.extensions.writert.alternative.alternative
import arrow.mtl.extensions.writert.applicative.applicative
import arrow.mtl.extensions.writert.divisible.divisible
import arrow.mtl.extensions.writert.eqK.eqK
import arrow.mtl.extensions.writert.functor.functor
import arrow.mtl.extensions.writert.monad.monad
import arrow.mtl.extensions.writert.monadFilter.monadFilter
import arrow.mtl.extensions.writert.monadTrans.monadTrans
import arrow.mtl.extensions.writert.monadWriter.monadWriter
import arrow.mtl.extensions.writert.monoidK.monoidK
import arrow.mtl.generators.GenTrans
import arrow.mtl.test.eq.eqK
import arrow.mtl.test.generators.genK
import arrow.typeclasses.EqK
import arrow.typeclasses.Monad
import io.kotlintest.properties.Gen

class WriterTTest : UnitSpec() {

  fun ioEQK(): WriterTEqK<ListK<Int>, ForIO> = WriterT.eqK(IO.eqK(), ListK.eq(Int.eq()))

  fun optionEQK(): WriterTEqK<ListK<Int>, ForOption> = WriterT.eqK(Option.eqK(), ListK.eq(Int.eq()))

  fun constEQK(): WriterTEqK<ListK<Int>, ConstPartialOf<Int>> = WriterT.eqK(Const.eqK(Int.eq()), ListK.eq(Int.eq()))

  fun listEQK(): WriterTEqK<ListK<Int>, ForListK> = WriterT.eqK(ListK.eqK(), ListK.eq(Int.eq()))

  init {

    testLaws(
      MonadTransControlLaws.laws(
        WriterT.monadTransControl(String.monoid()),
        object : GenTrans<Kind<ForWriterT, String>> {
          override fun <F> liftGenK(MF: Monad<F>, genK: GenK<F>): GenK<Kind<Kind<ForWriterT, String>, F>> =
            WriterT.genK(genK, Gen.string())
        },
        object : EqTrans<Kind<ForWriterT, String>> {
          override fun <F> liftEqK(MF: Monad<F>, eqK: EqK<F>): EqK<Kind<Kind<ForWriterT, String>, F>> =
            WriterT.eqK(eqK, String.eq())
        }
      ),
      MonadBaseControlLaws.laws(
        WriterT.monadBaseControl(String.monoid(), Id.monadBaseControl()),
        WriterT.genK(Id.genK(), Gen.string()),
        Id.genK(),
        WriterT.eqK(Id.eqK(), String.eq())
      ),
      AlternativeLaws.laws(
        WriterT.alternative(ListK.monoid<Int>(), Option.alternative()),
        WriterT.genK(Option.genK(), Gen.list(Gen.int()).map { it.k() }),
        optionEQK()
      ),
      DivisibleLaws.laws(
        WriterT.divisible<ListK<Int>, ConstPartialOf<Int>>(Const.divisible(Int.monoid())),
        WriterT.genK(Const.genK(Gen.int()), Gen.list(Gen.int()).map { it.k() }),
        constEQK()
      ),
      ConcurrentLaws.laws(
        WriterT.concurrent(IO.concurrent(), ListK.monoid<Int>()),
        WriterT.timer(IO.concurrent(), ListK.monoid<Int>()),
        WriterT.functor<ListK<Int>, ForIO>(IO.functor()),
        WriterT.applicative(IO.applicative(), ListK.monoid<Int>()),
        WriterT.monad(IO.monad(), ListK.monoid<Int>()),
        WriterT.genK(IO.genK(), Gen.list(Gen.int()).map { it.k() }),
        ioEQK()
      ),
      MonoidKLaws.laws(
        WriterT.monoidK<ListK<Int>, ForListK>(ListK.monoidK()),
        WriterT.genK(ListK.genK(), Gen.list(Gen.int()).map { it.k() }),
        listEQK()
      ),

      MonadWriterLaws.laws(
        WriterT.monad(Option.monad(), ListK.monoid<Int>()),
        WriterT.monadWriter(Option.monad(), ListK.monoid<Int>()),
        ListK.monoid<Int>(),
        WriterT.functor<ListK<Int>, ForOption>(Option.functor()),
        WriterT.applicative(Option.applicative(), ListK.monoid<Int>()),
        WriterT.monad(Option.monad(), ListK.monoid<Int>()),
        Gen.list(Gen.int()).map { it.k() },
        WriterT.genK(Option.genK(), Gen.list(Gen.int()).map { it.k() }),
        optionEQK(),
        ListK.eq(Int.eq())
      ),

      MonadFilterLaws.laws(
        WriterT.monadFilter(Option.monadFilter(), ListK.monoid<Int>()),
        WriterT.functor<ListK<Int>, ForOption>(Option.functor()),
        WriterT.applicative(Option.applicative(), ListK.monoid<Int>()),
        WriterT.monad(Option.monad(), ListK.monoid<Int>()),
        WriterT.genK(Option.genK(), Gen.list(Gen.int()).map { it.k() }),
        optionEQK()
      )
    )
  }
}

fun <W, F> WriterT.Companion.genK(
  GENKF: GenK<F>,
  GENW: Gen<W>
) = object : GenK<WriterTPartialOf<W, F>> {
  override fun <A> genK(gen: Gen<A>): Gen<Kind<WriterTPartialOf<W, F>, A>> =
    GENKF.genK(Gen.tuple2(GENW, gen)).map(::WriterT)
}
