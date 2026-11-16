// Offline content generation only. Does not connect to the database or run the application.
import fs from 'node:fs/promises';
import path from 'node:path';
import {fileURLToPath} from 'node:url';
const root=fileURLToPath(new URL('..',import.meta.url)), content=path.join(root,'src/main/resources/content');
const read=async p=>JSON.parse(await fs.readFile(p,'utf8'));
const write=async(p,data)=>fs.writeFile(p,JSON.stringify(data,null,2)+'\n');
const source=await read(path.join(root,'docs/final-nutrition-menu-source.json'));
const recipes=await read(path.join(content,'recipes.json'));
const foods=await read(path.join(content,'foods.json'));
const definitions=await read(path.join(content,'menus/index.json'));
const config=await read(path.join(root,'docs/final-nutrition-config.json'));
const {aliases,methodology,additionalFoods}=config;
for(const food of additionalFoods)if(!foods.some(f=>f.code===food.code))foods.push(food);
const foodMap=new Map(foods.map(f=>[f.code,f]));
const labels=['Закуска','Междинна закуска','Обяд','Следобедна закуска','Вечеря'];
const placeholder='/images/meals/meal-placeholder.svg';
const originalCodes=new Set(config.originalCodes);
const used=new Set(),created=new Set();
const signature=ingredients=>ingredients.map(i=>i.foodCode).sort().join('|');
function method(name){
 if(/палачинки/i.test(name))return 'pancake';
 if(/кюфт/i.test(name))return 'patty';
 if(/бургер/i.test(name))return 'burger';
 if(/шишчет/i.test(name))return 'skewer';
 if(/тортила/i.test(name))return 'wrap';
 if(/Бъркани яйца/i.test(name))return 'scrambled';
 if(/омлет/i.test(name))return 'omelette';
 if(/Яйца с домати на тиган/i.test(name))return 'tomato-eggs';
 if(/Яйца със сирене и спанак/i.test(name))return 'omelette';
 if(/Яйца с домати и кашкавал/i.test(name))return 'omelette';
 if(/тост|сандвич|сухари|яйца.*хляб|яйца.*тост/i.test(name))return 'toast';
 if(/хумус|дип/i.test(name))return 'dip';
 if(/^(?:Салата|Гръцка салата|Пилешка салата)/i.test(name))return 'salad';
 if(/паста|ориз|булгур|кус-кус|кускус/i.test(name)&&!/Овес|Кисело/.test(name))return 'grain';
 if(/Пилешко|Пилешка|Пуешко|Пуешки|Свинско|Свински|Телешко|Хек|риба|Пъстърва|скумрия|Лаврак|Сьомга|Тофу|Печени чушки|Боб с печени/i.test(name))return 'roast';
 return 'cold';
}
function ingredients(spec){return spec.split(/\s+/).map(token=>{
 const [key,amount]=token.split(':'),f=foodMap.get(aliases[key]);
 if(!f||!Number.isFinite(+amount)||+amount<=0)throw Error('Invalid ingredient '+token);
 return {name:f.name,grams:+amount,state:f.state,proteinPer100:f.protein,fatPer100:f.fat,carbsPer100:f.carbs,foodCode:f.code};
});}
function nutrition(items){
 let protein=0,fat=0,carbs=0;
 for(const i of items){protein+=i.grams*i.proteinPer100/100;fat+=i.grams*i.fatPer100/100;carbs+=i.grams*i.carbsPer100/100;}
 return {calories:Math.round(protein*4+fat*9+carbs*4),protein:Math.round(protein*10)/10,carbs:Math.round(carbs*10)/10,fat:Math.round(fat*10)/10,approximate:true};
}
function preparation(name,items){
 const keys=new Set(items.map(i=>Object.keys(aliases).find(k=>aliases[k]===i.foodCode)));
 const has=k=>keys.has(k),kind=method(name),steps=[];
 const meat=['chicken','turkey','pork','porkfillet','chop','beef'].find(has);
 const fish=['hake','whitefish','trout','mackerel','salmon','seabass'].find(has);
 const meatName=meat?foodMap.get(aliases[meat]).name.toLowerCase():'';
 const grain=['rice','bulgur','pasta','couscous'].find(has);
 const grainName=grain?foodMap.get(aliases[grain]).name.toLowerCase():'';
 let minutes=5;
 if(kind==='pancake'){
  steps.push(has('oats')?'Смели овеса и смеси с яйцата и посоченото мляко или извара.':'Смеси яйцата и изварата.');
  if(has('banana'))steps.push('Намачкай банана и го разбъркай в сместа.');
  if(has('apple'))steps.push('Настържи ябълката и я добави в сместа.');
  steps.push('Остави 5 минути. Намажи незалепващ тиган с цялото премерено количество зехтин и изпечи малки палачинки по 2–3 минути от страна до пълно стягане.');
  if(has('strawberry'))steps.push('Поднеси с нарязаните ягоди.');
  if(has('honey'))steps.push('Полей с измерения мед при сервиране.');
  minutes=25;
 }else if(['omelette','scrambled','tomato-eggs'].includes(kind)){
  if(has('potato')&&!name.includes('салата с картофи'))steps.push('Свари картофите на малки кубчета за 12–15 минути и отцеди. Добави ги в тигана преди яйцата.');
  let veg=items.filter(i=>['mushroom','pepper','spinach','tomato'].some(k=>aliases[k]===i.foodCode));
  if(name.includes('доматена салата')||name.includes('сирене, домати'))veg=veg.filter(i=>i.foodCode!==aliases.tomato);
  steps.push(veg.length?'Нарежи '+veg.map(i=>i.name.toLowerCase()).join(', ')+' и задуши 4–6 минути с посочения зехтин и 20 г вода.':'Загрей незалепващ тиган с посочения зехтин.');
  steps.push(kind==='tomato-eggs'?'Направи гнезда в доматите и добави яйцата; похлупи до пълно стягане.':kind==='scrambled'?'Разбий яйцата, добави в тигана и бъркай на слаб огън до пълно стягане.':'Разбий яйцата, излей в тигана и готви под капак на слаб огън до пълно стягане.');
  if(has('cheese')||has('yellowcheese')||has('feta'))steps.push('Добави посоченото сирене или кашкавал през последната минута.');
  if(has('bread'))steps.push('Поднеси с пълнозърнестия хляб.');
  if(has('avocado'))steps.push('Нарежи авокадото и поднеси към яйцата.');
  if(has('potato')&&name.includes('салата с картофи'))steps.push('Свари картофите на кубчета за 15–20 минути, охлади и поднеси като салата с краставицата.');
  if(has('lettuce')||has('cucumber')||name.includes('доматена салата')||name.includes('сирене, домати'))steps.push('Нарежи останалите свежи зеленчуци и поднеси отделно.');
  minutes=has('potato')?30:15;
 }else if(kind==='dip'){
  if(has('chickpea'))steps.push('Отцеди сварения нахут и пасирай с тахана, лимоновия сок, чесъна и 20 г вода до хумус.');
  else steps.push(has('cottage')?'Разбъркай изварата с киселото мляко и ситно нарязания копър.':'Разбъркай киселото мляко със ситно нарязания копър.');
  steps.push('Нарежи краставицата или морковите на пръчици и поднеси с дипа.');
  minutes=10;
 }else if(kind==='cold'){
  if(has('egg')){steps.push('Свари яйцето 9–10 минути след завиране, охлади и обели; количеството е без черупката.');minutes=15;}
  if(has('oats')&&has('milk')){steps.push('Загрей овеса с прясното мляко за 5–7 минути на слаб огън, като разбъркваш.');minutes=10;}
  else if(has('oats')){steps.push('Смеси овеса с млечната основа и остави 15 минути да омекне.');minutes=20;}
  if(has('chia')){steps.push('Разбъркай чиата в киселото мляко и остави 20 минути да набъбне; разбъркай отново.');minutes+=20;}
  const fresh=items.filter(i=>['Плодове','Зеленчуци'].includes(foodMap.get(i.foodCode)?.category));
  if(fresh.length)steps.push('Почисти и нарежи: '+fresh.map(i=>i.name.toLowerCase()).join(', ')+'.');
  steps.push('Претегли и поднеси заедно: '+items.map(i=>i.name.toLowerCase()).join(', ')+'.');
 }else{
  if(has('egg')&&!['toast','wrap','burger'].includes(kind)){steps.push('Свари яйцата 9–10 минути след завиране, охлади, обели и нарежи за салатата. Грамажът е за ядливата част без черупките.');minutes=20;}
  if(has('potato')){steps.push('Загрей фурната на 200°C. Нарежи картофите на кубчета, разпредели част от посочения зехтин и печи 30–35 минути.');minutes=40;}
  if(grain){steps.push(grain==='couscous'?'Залей сухия кускус с вряла вода според опаковката, похлупи за 5–7 минути и разрохкай.':grain==='pasta'?'Свари сухата паста във вода според опаковката за 8–12 минути и отцеди.':'Свари сухия '+grainName+' във вода според опаковката за '+(grain==='rice'?'15–20':'10–15')+' минути.');minutes=Math.max(minutes,25);}
  if(kind==='patty'||kind==='burger'){
   if(has('chickpea'))steps.push('Намачкай отцедения нахут, смеси със смления овес и 20 г вода и оформи малки плоски кюфтенца.');
   else steps.push('Смели '+meatName+' и омеси 1–2 минути с 20 г студена вода до свързване; оформи '+(kind==='burger'?'плоско кюфте за бургер.':'малки плоски кюфтета.'));
   steps.push('Изпечи в незалепващ тиган с част от посочения зехтин по 5–7 минути от страна до пълна готовност.');minutes=Math.max(minutes,30);
  }else if(kind==='skewer'){
   steps.push('Нарежи '+meatName+' на еднакви кубчета, нанижи на шишчета и намажи с част от премерения зехтин. Печи при 200°C за 18–25 минути, като обърнеш веднъж.');minutes=Math.max(minutes,35);
  }else if(meat){
   steps.push('Нарежи '+meatName+' на тънки ивици и готви в незалепващ тиган с част от посочения зехтин и 30 г вода за '+(['chicken','turkey'].includes(meat)?'10–12':'12–15')+' минути до пълна готовност.');minutes=Math.max(minutes,25);
  }
  if(fish){steps.push('Изпечи рибното филе при 200°C за 15–20 минути до непрозрачна среда и лесно разделяне на люспи. Използвай само част от общия посочен зехтин.');minutes=Math.max(minutes,30);}
  if(has('shrimp')){steps.push('Задуши чесъна за 30 секунди с посочения зехтин, добави доматите за 6–8 минути и скаридите за още 3–5 минути до непрозрачност.');minutes=Math.max(minutes,25);}
  if(has('tofu')){steps.push('Подсуши тофуто, нарежи на кубчета и запечи с част от посочения зехтин в тиган за 8–10 минути.');minutes=Math.max(minutes,25);}
  if(kind==='toast'||kind==='burger'){
   if(has('egg'))steps.push('Свари яйцата 9–10 минути след завиране, охлади, обели и нарежи.');
   steps.push(has('rusk')?'Намажи сухарите с крема сиренето.':'Препечи хляба или питката в сух тостер и подреди '+items.filter(i=>!['bread','bun','oil'].some(k=>aliases[k]===i.foodCode)).map(i=>i.name.toLowerCase()).join(', ')+'.');
   if(has('yellowcheese'))steps.push('Запечи кашкавала върху тостовете за 2–3 минути под грил.');
   minutes=Math.max(minutes,has('egg')?20:10);
  }
  if(has('cauliflower')){
   steps.push(name.includes('пюре')?'Свари карфиола за 10–12 минути, отцеди и пасирай с посоченото мляко и част от измерения зехтин.':'Накъсай карфиола и свари на пара за 10–12 минути.');minutes=Math.max(minutes,25);
  }
  if(has('broccoli'))steps.push('Свари броколите на пара за 6–8 минути.');
  if(has('greenbean'))steps.push('Задуши зеления фасул с 80 г вода под капак за 15–20 минути до омекване.');
  const vegetables=['zucchini','pepper','carrot','mushroom','spinach'].filter(has);
  if(vegetables.length){
   const names=vegetables.map(k=>foodMap.get(aliases[k]).name.toLowerCase()).join(', ');
   if(/печени (?:зеленчуци|тиквички|чушки|моркови)|с печени чушки|с печени тиквички/i.test(name)){
    steps.push('Нарежи '+names+' и изпечи при 200°C с част от измерения зехтин за 20–25 минути.');minutes=Math.max(minutes,35);
   }else if(!name.includes('тортила')&&!name.includes('Тортила'))steps.push('Нарежи '+names+' и задуши в тиган с част от измерения зехтин и 30 г вода за 7–10 минути.');
  }
  if(has('tomato')&&(/доматен сос|Паста с|Паста със|паста с/.test(name))&&!has('shrimp'))steps.push('Нарежи доматите ситно и остави да къкрят 8–10 минути с част от измерения зехтин; смеси със сварената паста.');
  if(has('cream'))steps.push('Добави сметаната към месото и гъбите и остави на слаб огън за още 3–4 минути.');
  if(has('chickpea')&&!['patty','dip'].includes(kind)||has('bean')||has('lentil')||has('peas')||has('corn'))steps.push(kind==='salad'?'Отцеди сварените бобови и смеси със салатата.':'Отцеди посочените сварени бобови, грах или царевица и затопли в тигана за 2–3 минути.');
  if(has('tuna'))steps.push('Отцеди рибата тон и добави непосредствено преди сервиране.');
  if(kind==='wrap'){
   if(has('egg'))steps.push('Разбий яйцата и готви в тиган с посочения зехтин до пълно стягане.');
   steps.push('Затопли тортилата за 30 секунди в сух тиган. Нарежи свежите зеленчуци, добави готовата плънка и завий.');minutes=Math.max(minutes,20);
  }
  if(has('yogurt')){
   steps.push(has('garlic')&&has('cucumber')?'Настържи част от краставицата, отцеди и смеси с млякото, чесъна и копъра за дзадзики.':'Разбъркай киселото мляко за сос и поднеси към готовото ястие.');
  }
  if(has('lettuce')||has('cucumber')||has('cabbage')||has('tomato')&&!/доматен сос|Паста с|паста с|скариди/i.test(name))steps.push('Нарежи свежите зеленчуци за салатата или плънката и поднеси с останалите продукти.');
  if(has('cheese')||has('feta')||has('mozzarella')||has('yellowcheese'))steps.push('Добави посоченото сирене към готовото ястие; кашкавала настържи, а моцарелата остави да се разтопи за 1–2 минути.');
  if(has('olive')||has('avocado')||has('lemon')||has('parsley')||has('oregano'))steps.push('Добави посочените маслини, авокадо, лимонов сок или зелени подправки при сервиране.');
  if(has('oil'))steps.push('Разпредели само общото премерено количество зехтин между готвенето и салатата.');
 }
 if(meat)steps.push(['chicken','turkey'].includes(meat)?'Птичето месо трябва да е напълно сготвено в центъра.':'Готви месото до пълна готовност; цели парчета остави да починат 3 минути.');
 return {steps,minutes};
}
const menus=[];
for(const plan of source){
 const days=plan.days.map(day=>({title:day.title,meals:day.specs.map((entry,slot)=>{
  const items=ingredients(entry.spec),prep=preparation(entry.name,items),sig=signature(items),kind=method(entry.name);
  // Composition and cooking method both have to match. Portions do not define recipe identity.
  let recipe=recipes.find(r=>signature(r.meal.ingredients)===sig&&method(r.name)===kind);
  if(!recipe){
   const next=Math.max(...recipes.map(r=>+r.code.slice(7)))+1;
   recipe={code:'recipe-'+String(next).padStart(3,'0'),name:entry.name,label:labels[slot],imagePath:placeholder,preparationMinutes:prep.minutes,steps:prep.steps,meal:null};
   recipes.push(recipe);created.add(recipe.code);
  }
  used.add(recipe.code);
  const meal={label:labels[slot],name:entry.name,ingredients:items,preparation:prep.steps.join(' '),substitution:'Порцията е за този режим. Хранителните стойности са ориентировъчни; промяна на съставките или грамажите изисква преизчисляване.',recipeCode:recipe.code,nutrition:nutrition(items)};
  if(!recipe.meal)recipe.meal=structuredClone(meal);
  return meal;
 })}));
 menus.push({code:plan.code,menu:{author:'FitPulse',methodology,days}});
}
// Preserve every historical recipe and code. Reviewed photos are explicit, never inferred from filenames.
for(const recipe of recipes){
 const reviewed=config.reviewedImages[recipe.code];
 recipe.imagePath=reviewed||placeholder;
 recipe.nutrition=nutrition(recipe.meal.ingredients);
 // New recipes and reused recipes get precise, composition-specific instructions.
 if(used.has(recipe.code)){
  const first=menus.flatMap(p=>p.menu.days.flatMap(d=>d.meals)).find(m=>m.recipeCode===recipe.code);
  recipe.label=first.label;recipe.meal=structuredClone(first);
  recipe.nutrition=nutrition(first.ingredients);
  const prep=preparation(first.name,first.ingredients);
  recipe.steps=prep.steps;recipe.preparationMinutes=prep.minutes;
  recipe.meal.preparation=prep.steps.join(' ');
 }
}
await write(path.join(content,'recipes.json'),recipes);
await write(path.join(content,'foods.json'),foods);
for(const {code,menu} of menus)await write(path.join(content,'menus',code+'.json'),menu);
// Content generation must not rewrite already applied Flyway migrations.
const missing=recipes.filter(r=>r.imagePath===placeholder);
const activeMissing=missing.filter(r=>used.has(r.code));
await fs.writeFile(path.join(root,'docs/missing-recipe-images.md'),'# Рецепти без подходяща снимка\n\nИзползва се неутралният заместител `meal-placeholder.svg`. Дизайнът и кориците на останалите режими са запазени. Не са генерирани или изтегляни изображения. Списъкът включва и запазените исторически рецепти; всяка идентичност е посочена веднъж.\n\nВ окончателните менюта: '+activeMissing.length+' рецепти без подходяща снимка. В целия запазен каталог: '+missing.length+'.\n\n'+missing.map(r=>'- `'+r.code+'` — '+r.name+(used.has(r.code)?' (окончателни менюта)':' (запазена историческа рецепта)')).join('\n')+'\n');
const summary={existingRecipesUsed:[...used].filter(c=>originalCodes.has(c)).length,newRecipesCreated:recipes.filter(r=>!originalCodes.has(r.code)).length,uniqueRecipesInFinalMenus:used.size,totalCatalogRecipes:recipes.length,missingImagesInFinalMenus:activeMissing.length,missingImagesInCatalog:missing.length,migration:'V19__remove_mediterranean_meal_plan.sql',migrationApplied:false,v17Status:'applied; unchanged',plans:menus.map(({code,menu})=>({code,days:menu.days.length,meals:menu.days.reduce((n,d)=>n+d.meals.length,0),totals:menu.days.map(d=>nutrition(d.meals.flatMap(m=>m.ingredients)))})),reusedCodes:[...used].filter(c=>originalCodes.has(c))};
await write(path.join(root,'docs/nutrition-v2-summary.json'),summary);
console.log(JSON.stringify({...summary,plans:summary.plans.map(({code,days,meals})=>({code,days,meals}))},null,2));
